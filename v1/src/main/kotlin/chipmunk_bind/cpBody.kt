package chipmunk_bind

import chipmunk_bind.cpVect.Companion.cpvlengthsq
import chipmunk_bind.cpVect.Companion.cpvzero


interface IBodyFunc {
    fun cpBodyVelocityFunc(gravity: cpVect, damping: Float, dt: Float)
}


enum class cpBodyType {
    /// A dynamic body is one that is affected by gravity, forces, and collisions.
    /// This is the default body type.
    CP_BODY_TYPE_DYNAMIC,
    /// A kinematic body is an infinite mass, user controlled body that is not affected by gravity, forces or collisions.
    /// Instead the body only moves based on it's velocity.
    /// Dynamic bodies collide normally with kinematic bodies, though the kinematic body will be unaffected.
    /// Collisions between two kinematic bodies, or a kinematic body and a static body produce collision callbacks, but no collision response.
    CP_BODY_TYPE_KINEMATIC,
    /// A static body is a body that never (or rarely) moves. If you move a static body, you must call one of the cpSpaceReindex*() functions.
    /// Chipmunk uses this information to optimize the collision detection.
    /// Static bodies do not produce collision callbacks when colliding with other static bodies.
    CP_BODY_TYPE_STATIC,
}

//*/// Rigid body velocity update function type.
interface ICPBodyVelocityFunc {
    fun perform(gravity: cpVect, damping: Float, dt: Float)
}

//typedef void (*cpBodyVelocityFunc)(cpBody *body, cpVect gravity, cpFloat damping, cpFloat dt);
///// Rigid body position update function type.
//typedef void (*cpBodyPositionFunc)(cpBody *body, cpFloat dt);*/
interface ICPBodyPositionFunc {
    fun perform(dt: Float)
}

// Integration functions


class cpBody : cpObject() {

    var velocity_func: ICPBodyVelocityFunc? = null
    var position_func: ICPBodyPositionFunc? = null

    // mass and it's inverse
    var m: Float = 0.0f
    var m_inv: Float = 0.0f

    // moment of inertia and it's inverse
    private var i: Float = 0.0f
    var i_inv: Float = 0.0f

    // center of gravity
    var cog = cpVect()

    // position, velocity, force
    var p = cpVect.cpvzero
    var v = cpVect.cpvzero
    private var f = cpVect.cpvzero

    // Angle, angular velocity, torque (radians)
    private var a: Float = 0f
    var w: Float = 0f
    private var t: Float = 0f

    var transform = cpTransform()

//    cpDataPointer userData;

    // "pseudo-velocities" used for eliminating overlap.
    // Erin Catto has some papers that talk about what these are.
    var v_bias: cpVect = cpVect.cpvzero
    var w_bias: Float = 0f

    var space: cpSpace? = null

    //    ???
    var shapeList: ArrayList<cpShape> = ArrayList()

    var arbiterList: ArrayList<cpArbiter> = ArrayList() //cpArbiter *arbiterList;

    var constraintList: ArrayList<cpConstraint> = ArrayList()

    //
    class Sleeping {
        var root: cpBody? = null
        var next: cpBody? = null
        var idleTime: Float = 0.0f
    }

    var sleeping: Sleeping = Sleeping()


    fun cpBodyInit(mass: Float, moment: Float) {
        space = null
        shapeList.clear()

        velocity_func = object : ICPBodyVelocityFunc {
            override fun perform(gravity: cpVect, damping: Float, dt: Float) {
                // Skip kinematic bodies.
                if (cpBodyGetType() == cpBodyType.CP_BODY_TYPE_KINEMATIC) return

//                cpAssertSoft(body->m > 0.0f && body->i > 0.0f, "Body's mass and moment must be positive to simulate. (Mass: %f Moment: %f)", body->m, body->i);

                val par1 = cpVect.cpvmult(v, damping)
                val par2 = cpVect.cpvmult(f, m_inv)
                val par3 = cpVect.cpvadd(gravity, par2)
                val par4 = cpVect.cpvmult(par3, dt)
                v = cpVect.cpvadd(par1, par4)



                w = w * damping + t * i_inv * dt

                // Reset forces.
                f = cpvzero
                t = 0.0f

                cpBodySanityCheck()
            }

        }
        position_func = object : ICPBodyPositionFunc {
            override fun perform(dt: Float) {
                p = cpVect.cpvadd(p, cpVect.cpvmult(cpVect.cpvadd(v, v_bias), dt))
                val a = SetAngle(a + (w + w_bias) * dt)
                SetTransform(p, a)

                v_bias = cpvzero

                w_bias = 0.0f

                cpBodySanityCheck()
            }

        }

        // Setters must be called after full initialization so the sanity checks don't assert on garbage data.
        cpBodySetMass(mass)
        cpBodySetMoment(moment)
        cpBodySetAngle(0.0f)
    }

    fun cpBodySetMoment(moment: Float) {
        AssertionError(moment >= 0.0f) //cpAssertHard(moment >= 0.0f, "Moment of Inertia must be positive.");

        cpBodyActivate()
        i = moment
        i_inv = 1.0f / moment
        cpBodySanityCheck()
    }

    fun cpBodySetMass(mass: Float) {
//        cpAssertHard(cpBodyGetType() == CP_BODY_TYPE_DYNAMIC, "You cannot set the mass of kinematic or static bodies.");
//        cpAssertHard(0.0f <= mass && mass < INFINITY, "Mass must be positive and finite.");

        cpBodyActivate()
        m = mass
        m_inv = 1.0f / mass
        cpBodySanityCheck()
    }

    fun cpBodySetAngle(angle: Float) {
        cpBodyActivate()
        SetAngle(angle)
        SetTransform(p, angle)
    }

    // 'p' is the position of the CoG
    fun SetTransform(p: cpVect, a: Float) {
        val rot = cpVect.cpvforangle(a)
        val c = cog

        transform = cpTransformNewTranspose(
                rot.x, -rot.y, p.x - (c.x * rot.x - c.y * rot.y),
                rot.y, rot.x, p.y - (c.x * rot.y + c.y * rot.x)
        )
    }

    fun SetAngle(a: Float): Float {
        this.a = a
        cpBodySanityCheck()
        return a
    }

    fun cpBodyActivate() {
        if (cpBodyGetType() == cpBodyType.CP_BODY_TYPE_DYNAMIC) {
            sleeping.idleTime = 0.0f

            val root = ComponentRoot()
            if (root != null && cpBodyIsSleeping(root)) {
                // TODO should cpBodyIsSleeping(root) be an assertion?
//                cpAssertSoft(cpBodyGetType(root) == CP_BODY_TYPE_DYNAMIC, "Internal Error: Non-dynamic body component root detected.");

                val space = root.space
                var body = root
                while (body != null) {
                    val next = body.sleeping.next

                    body.sleeping.idleTime = 0.0f
                    body.sleeping.root = null
                    body.sleeping.next = null
                    space?.cpSpaceActivateBody(body)

                    body = next
                }

                space?.let {
                    it.sleepingComponents.remove(root)
                }
            }

            arbiterList.forEach {
                // Reset the idle timer of things the body is touching as well.
                // That way things don't get left hanging in the air.
                val other = if (it.body_a == this) it.body_b else it.body_a
                if (other!!.cpBodyGetType() != cpBodyType.CP_BODY_TYPE_STATIC) other.sleeping.idleTime = 0.0f
            }

        }
    }

    fun cpBodyActivateStatic(filter: cpShape) {

        arbiterList.forEach { it ->
            if (filter == it.a || filter == it.b) {
                (if (it.body_a == this) it.body_b else it.body_a)?.cpBodyActivate()
            }
        }

        // TODO: should also activate joints?
    }

    fun cpBodySetPosition(position: cpVect) {
        cpBodyActivate()
        p = cpVect.cpvadd(cpTransformVect(transform, cog), position)
        cpBodySanityCheck()

        SetTransform(p, a)
    }

    fun ComponentRoot(): cpBody? {
        return sleeping.root
    }

    fun cpBodyGetType(): cpBodyType {
        if (sleeping.idleTime == INFINITY) {
            return cpBodyType.CP_BODY_TYPE_STATIC
        } else if (m == INFINITY) {
            return cpBodyType.CP_BODY_TYPE_KINEMATIC
        } else {
            return cpBodyType.CP_BODY_TYPE_DYNAMIC
        }
    }

    fun cpBodySanityCheck() {
//        cpAssertHard(body->m == body->m && body->m_inv == body->m_inv, "Body's mass is NaN.");
//        cpAssertHard(body->i == body->i && body->i_inv == body->i_inv, "Body's moment is NaN.");
//        cpAssertHard(body->m >= 0.0f, "Body's mass is negative.");
//        cpAssertHard(body->i >= 0.0f, "Body's moment is negative.");
//
//        cpv_assert_sane(body->p, "Body's position is invalid.");
//        cpv_assert_sane(body->v, "Body's velocity is invalid.");
//        cpv_assert_sane(body->f, "Body's force is invalid.");
//
//        cpAssertHard(body->a == body->a && cpfabs(body->a) != INFINITY, "Body's angle is invalid.");
//        cpAssertHard(body->w == body->w && cpfabs(body->w) != INFINITY, "Body's angular velocity is invalid.");
//        cpAssertHard(body->t == body->t && cpfabs(body->t) != INFINITY, "Body's torque is invalid.");
    }

    fun cpBodySetType(type: cpBodyType) {
        val oldType = cpBodyGetType()
        if (oldType == type) return

        // Static bodies have their idle timers set to infinity.
        // Non-static bodies should have their idle timer reset.
        sleeping.idleTime = (if (type == cpBodyType.CP_BODY_TYPE_STATIC) INFINITY else 0.0f)

        if (type == cpBodyType.CP_BODY_TYPE_DYNAMIC) {
            m = 0.0f
            i = 0.0f
            m_inv = INFINITY
            i_inv = INFINITY

            cpBodyAccumulateMassFromShapes()
        } else {
            m = INFINITY
            i = INFINITY
            m_inv = 0f
            i_inv = 0.0f

            v = cpVect.cpvzero
            w = 0.0f
        }

        // If the body is added to a space already, we'll need to update some space data structures.
        val space = space
        if (space != null) {
//            cpAssertSpaceUnlocked(space);

            if (oldType == cpBodyType.CP_BODY_TYPE_STATIC) {
                // TODO This is probably not necessary
//			    cpBodyActivateStatic(null)
            } else {
                cpBodyActivate()
            }

            // Move the bodies to the correct array.
            val fromArray = space.cpSpaceArrayForBodyType(oldType)
            val toArray = space.cpSpaceArrayForBodyType(type)
            if (fromArray != toArray) {
                if (fromArray.contains(this))
                    fromArray.remove(this)
                if (!toArray.contains(this))
                    toArray.add(this)
            }

            // Move the body's shapes to the correct spatial index.
            val fromIndex = if (oldType == cpBodyType.CP_BODY_TYPE_STATIC) space.staticShapes else space.dynamicShapes
            val toIndex = if (type == cpBodyType.CP_BODY_TYPE_STATIC) space.staticShapes else space.dynamicShapes
            if (fromIndex != toIndex) {
                shapeList.forEach {
                    fromIndex.remove(it, it.hashid)
                    toIndex.insert(it, it.hashid)
                }
            }
        }
    }


    fun cpBodyGetPosition(): cpVect {
        return cpTransformPoint(transform, cpVect.cpvzero)
    }

    fun cpBodyAddShape(shape: cpShape) {
        shapeList.add(shape)
        shape.massInfo?.let {
            if (it.m > 0.0f) {
                cpBodyAccumulateMassFromShapes()
            }
        }
    }

    fun cpBodyIsSleeping(body: cpBody): Boolean {
        return body.sleeping.root != null
    }

    fun cpBodyAccumulateMassFromShapes() {
        if (cpBodyGetType() != cpBodyType.CP_BODY_TYPE_DYNAMIC) return

        // Reset the body's mass data.
        m = 0.0f
        i = 0.0f
        cog = cpVect.cpvzero

        // Cache the position to realign it at the end.
        val pos = cpBodyGetPosition()

        // Accumulate mass from shapes.
        shapeList.forEach {
            val info = it.massInfo
            val tmp_m = info.m

            if (tmp_m > 0.0f) {
                val msum = m + tmp_m

                i += tmp_m * info.i + cpVect.cpvdistsq(cog, info.cog) * (tmp_m * m) / msum
                cog = cpVect.cpvlerp(cog, info.cog, tmp_m / msum)
                m = msum
            }
        }


        // Recalculate the inverses.
        m_inv = 1.0f / m
        i_inv = 1.0f / i

        // Realign the body since the CoG has probably moved.
        cpBodySetPosition(pos)
        cpBodySanityCheck()
    }

    fun cpBodyGetCenterOfGravity() = cog

    fun cpBodySetTorque(torque: Float) {
        cpBodyActivate()
        t = torque
        cpBodySanityCheck()
    }

    fun cpBodySetCenterOfGravity(cog: cpVect) {
        cpBodyActivate()
        this.cog = cog
        cpBodySanityCheck()
    }


    fun cpBodyRemoveShape(shape: cpShape) {
        val prev = shape.prev
        val next = shape.next

        if (prev != null) {
            prev.next = next
        }

        if (shapeList.contains(shape))
            shapeList.remove(shape)

        if (next != null) {
            next.prev = prev
        }

        shape.prev = null
        shape.next = null

        if (cpBodyGetType() == cpBodyType.CP_BODY_TYPE_DYNAMIC && shape.massInfo.m > 0.0f) {
            cpBodyAccumulateMassFromShapes()
        }
    }

    fun cpBodyPushArbiter(arb: cpArbiter) {
//        cpAssertSoft(cpArbiterThreadForBody(arb, body)->next == NULL, "Internal Error: Dangling contact graph pointers detected. (A)");
//        cpAssertSoft(cpArbiterThreadForBody(arb, body)->prev == NULL, "Internal Error: Dangling contact graph pointers detected. (B)");

//        cpArbiter *next = body->arbiterList;
//        cpAssertSoft(next == NULL || cpArbiterThreadForBody(next, body)->prev == NULL, "Internal Error: Dangling contact graph pointers detected. (C)");
//        arb.cpArbiterThreadForBody(this)?.next = next;
//
//        if(next) cpArbiterThreadForBody(next, body)->prev = arb;
//        body->arbiterList = arb;

        arbiterList.add(arb)
    }

    fun cpBodyKineticEnergy(): Float {
        // Need to do some fudging to avoid NaNs
        val vsq = cpVect.cpvdot(v, v)
        val wsq = w * w
        return if (vsq != 0f) vsq * m else 0.0f + if (wsq != 0f) wsq * i else 0.0f
    }


    fun ComponentActive(threshold: Float): Boolean {

        var other: cpBody? = this
        do {
            other = other?.sleeping?.next
            if (other != null) {
                if (other.sleeping.idleTime < threshold) return true
            }
        } while (other != null)

        return false
    }

    fun FloodFillComponent(body: cpBody) {
        // Kinematic bodies cannot be put to sleep and prevent bodies they are touching from sleeping.
        // Static bodies are effectively sleeping all the time.
        if (body.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_DYNAMIC) {
            val other_root = body.ComponentRoot()
            if (other_root == null) {
                ComponentAdd(this, body)
                arbiterList.forEach { arb ->
                    FloodFillComponent(if (body == arb.body_a) arb.body_b!! else arb.body_a!!)

                }
                constraintList.forEach { constraint ->
                    FloodFillComponent(if (body == constraint.a) constraint.b else constraint.a)
                }

            } else {
//                cpAssertSoft(other_root == root, "Internal Error: Inconsistency dectected in the contact graph.");
            }
        }
    }

    fun ComponentAdd(root: cpBody, body: cpBody) {

        sleeping.root = root

        if (body != root) {
            body.sleeping.next = root.sleeping.next
            root.sleeping.next = body
        }
    }
}

fun cpArrayDeleteObj(arr: Array<cpObject?>, obj: cpObject) {
    for (i in 0 until arr.size) {
        if (arr[i] == null)
            return
        if (arr[i] == obj) {
            val size = arr.size - 1

            arr[i] = arr[size]
            arr[arr.size] = null

            return
        }
    }
}


fun cpTransformNewTranspose(a: Float, c: Float, tx: Float, b: Float, d: Float, ty: Float): cpTransform {
    return cpTransform(a, b, c, d, tx, ty)
}


/// Transform a vector (i.e. a normal)
fun cpTransformVect(t: cpTransform, v: cpVect): cpVect {
    return cpVect(t.a * v.x + t.c * v.y, t.b * v.x + t.d * v.y)
}

fun cpMomentForCircle(m: Float, r1: Float, r2: Float, offset: cpVect = cpVect.cpvzero): Float {
    return m * (0.5f * (r1 * r1 + r2 * r2) + cpvlengthsq(offset))
}

fun cpArbiterNext(node: cpArbiter, body: cpBody): cpArbiter? {
    return if (node.body_a == body) node.thread_a?.next else node.thread_b?.next
}

/*#define CP_BODY_FOREACH_ARBITER(bdy, var)\
	for(cpArbiter *var = bdy->arbiterList; var; var = cpArbiterNext(var, bdy))

#define CP_BODY_FOREACH_SHAPE(body, var)\
	for(cpShape *var = body->shapeList; var; var = var->next)

#define CP_BODY_FOREACH_COMPONENT(root, var)\
	for(cpBody *var = root; var; var = var->sleeping.next)
*/