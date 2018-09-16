package chipmunk_bind

abstract class cpObject

data class cpTransform(
        var a: Float = 0f,
        var b: Float = 0f,
        var c: Float = 0f,
        var d: Float = 0f,
        var tx: Float = 0f,
        var ty: Float = 0f
) {
    companion object {
        fun identity(): cpTransform {
            return cpTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f)
        }
    }

}

const val CP_PI = 3.14159265358979323846264338327950288.toFloat()
const val INFINITY = (1e1000).toFloat()
// TODO: Eww. Magic numbers.
const val MAGIC_EPSILON = (1e-5).toFloat()
const val CP_WILDCARD_COLLISION_TYPE = 0.inv()

enum class cpArbiterState {
    // Arbiter is active and its the first collision.
    CP_ARBITER_STATE_FIRST_COLLISION,
    // Arbiter is active and its not the first collision.
    CP_ARBITER_STATE_NORMAL,
    // Collision has been explicitly ignored.
    // Either by returning false from a begin collision handler or calling cpArbiterIgnore().
    CP_ARBITER_STATE_IGNORE,
    // Collison is no longer active. A space will cache an arbiter for up to cpSpace.collisionPersistence more steps.
    CP_ARBITER_STATE_CACHED,
    // Collison arbiter is invalid because one of the shapes was removed.
    CP_ARBITER_STATE_INVALIDATED,
}

data class cpGroup(val groupId: Int) {
    companion object {
        val CP_NO_GROUP = cpGroup(0)
    }
}

data class cpBitmask(val mask: Int) {
    companion object {
        val CP_ALL_CATEGORIES = cpBitmask(0)
    }
}

data class cpCollisionType(val type: Int)

/// Point query info struct.
data class cpPointQueryInfo(
        /// The nearest shape, NULL if no shape was within range.
        var shape: cpShape? = null,
        /// The closest point on the shape's surface. (in world space coordinates)
        var point: cpVect = cpVect.cpvzero,
        /// The distance to the point. The distance is negative if the point is inside the shape.
        var distance: Float = 0f,
        /// The gradient of the signed distance function.
        /// The value should be similar to info.p/info.d, but accurate even for very small values of info.d.
        var gradient: cpVect = cpVect.cpvzero
)

interface cpSpacePointQueryFunc {
    fun query(shape: cpShape, point: cpVect, distance: Float, gradient: cpVect, data: Any)
}

data class PointQueryContext(var point: cpVect, var maxDistance: Float, var filter: cpShapeFilter, var cb: cpSpacePointQueryFunc? = null)


//
class cpArbiterThread {
    var next: cpArbiter? = null
    var prev: cpArbiter? = null
}

class cpContact {
    var r1: cpVect = cpVect.cpvzero
    var r2: cpVect = cpVect.cpvzero

    var nMass: Float = 0f
    var tMass: Float = 0f
    var bounce: Float = 0f

    var jnAcc: Float = 0f
    var jtAcc: Float = 0f
    var jBias: Float = 0f

    var bias: Float = 0f

    //    cpHashValue hash;
    var hash: Int = 0
};
//
//struct cpCollisionInfo {
//    const cpShape *a, *b;
//    cpCollisionID id;
//
//    cpVect n;
//
//    int count;
//    // TODO Should this be a unique struct type?
//    struct cpContact *arr;
//};

/// Collision begin event function callback type.
/// Returning false from a begin callback causes the collision to be ignored until
/// the the separate callback is called when the objects stop colliding.
interface ICPCollisionBeginFunc {
    fun perform(arb: cpArbiter, space: cpSpace, userData: Any?): Boolean
}/// Collision pre-solve event function callback type.

/// Returning false from a pre-step callback causes the collision to be ignored until the next step.
interface ICPCollisionPreSolveFunc {
    fun perform(arb: cpArbiter, space: cpSpace, userData: Any?): Boolean
}

/// Collision post-solve event function callback type.
interface ICPCollisionPostSolveFunc {
    fun perform(arb: cpArbiter, space: cpSpace, userData: Any?)
}

/// Collision separate event function callback type.
interface ICPCollisionSeparateFunc {
    fun perform(arb: cpArbiter, space: cpSpace, userData: Any?)
}

class cpCollisionHandler(
//    /// Collision type identifier of the first shape that this handler recognizes.
//    /// In the collision handler callback, the shape with this type will be the first argument. Read only.
        val typeA: cpCollisionType,
//    /// Collision type identifier of the second shape that this handler recognizes.
//    /// In the collision handler callback, the shape with this type will be the second argument. Read only.
        val typeB: cpCollisionType,
//    /// This function is called when two shapes with types that match this collision handler begin colliding.
        var beginFunc: ICPCollisionBeginFunc? = null,
//    /// This function is called each step when two shapes with types that match this collision handler are colliding.
//    /// It's called before the collision solver runs so that you can affect a collision's outcome.
        var preSolveFunc: ICPCollisionPreSolveFunc? = null,
//    /// This function is called each step when two shapes with types that match this collision handler are colliding.
//    /// It's called after the collision solver runs so that you can read back information about the collision to trigger events in your game.
        var postSolveFunc: ICPCollisionPostSolveFunc? = null,
//    /// This function is called when two shapes with types that match this collision handler stop colliding.
        var separateFunc: ICPCollisionSeparateFunc? = null,
//    /// This is a user definable context pointer that is passed to all of the collision handler functions.
        var userData: Any? = null
);

class cpArbiter(val a: cpShape, val b: cpShape) {
    var e: Float = 0f
    var u: Float = 0f
    var surface_vr = cpVect.cpvzero

    var data: Any? = null

    var body_a: cpBody? = null
    var body_b: cpBody? = null

    var thread_a: cpArbiterThread? = null
    var thread_b: cpArbiterThread? = null

    var count: Int = 0
    var contacts: Array<cpContact> = emptyArray()

    var n: cpVect = cpVect.cpvzero

    // Regular, wildcard A and wildcard B collision handlers.
    var handler: cpCollisionHandler? = null
    var handlerA: cpCollisionHandler? = null
    var handlerB: cpCollisionHandler? = null

    var swapped: Boolean = false

    //    cpTimestamp stamp;
    var stamp: Int = 0
    var state: cpArbiterState = cpArbiterState.CP_ARBITER_STATE_NORMAL

    fun cpArbiterThreadForBody(body: cpBody): cpArbiterThread? {
        return if (body_a == body) thread_a else thread_b
    }

    fun cpArbiterCallWildcardBeginA(space: cpSpace): Boolean {
        return handlerA?.beginFunc?.perform(this, space, handlerA?.userData) == true
    }

    fun cpArbiterCallWildcardBeginB(space: cpSpace): Boolean {
        swapped = !swapped
        val retval = (handlerB?.beginFunc?.perform(this, space, handlerB?.userData) == true)
        swapped = !swapped
        return retval
    }

    fun cpArbiterCallWildcardPreSolveA(space: cpSpace): Boolean {
        return handlerA?.preSolveFunc?.perform(this, space, handlerA?.userData) == true
    }

    fun cpArbiterCallWildcardPreSolveB(space: cpSpace): Boolean {
        swapped = !swapped
        val retval = handlerB?.preSolveFunc?.perform(this, space, handlerB?.userData) == true
        swapped = !swapped
        return retval
    }

    fun cpArbiterCallWildcardPostSolveA(space: cpSpace) {
        handlerA?.postSolveFunc?.perform(this, space, handlerA?.userData)
    }

    fun cpArbiterCallWildcardPostSolveB(space: cpSpace) {
        swapped = !swapped
        handlerB?.postSolveFunc?.perform(this, space, handlerB?.userData)
        swapped = !swapped
    }

    fun cpArbiterCallWildcardSeparateA(space: cpSpace) {
        handlerA?.separateFunc?.perform(this, space, handlerA?.userData)
    }

    fun cpArbiterCallWildcardSeparateB(space: cpSpace) {
        swapped = !swapped
        handlerB?.separateFunc?.perform(this, space, handler?.userData)
        swapped = !swapped
    }

    fun cpArbiterUnthread() {
        unthreadHelper(body_a!!)
        unthreadHelper(body_b!!)
    }

    fun unthreadHelper(body: cpBody) {
        val thread = cpArbiterThreadForBody(body)
        val prev = thread?.prev
        val next = thread?.next

//        if(prev != null){
//            cpArbiterThreadForBody(prev, body)->next = next;
//        } else if(body->arbiterList == arb) {
//        // IFF prev is NULL and body->arbiterList == arb, is arb at the head of the list.
//        // This function may be called for an arbiter that was never in a list.
//        // In that case, we need to protect it from wiping out the body->arbiterList pointer.
//        body->arbiterList = next;
//    }
//
//        if(next) cpArbiterThreadForBody(next, body)->prev = prev;
//
//        thread->prev = NULL;
//        thread->next = NULL;
    }

    fun cpArbiterPreStep(dt: Float, slop: Float, bias: Float) {
        val a = body_a!!
        val b = body_b!!
        val n = n
        val body_delta = cpVect.cpvsub(b.p, a.p)

        for (i in 0 until count) {
            val con = contacts[i]

            // Calculate the mass normal and mass tangent.
            con.nMass = 1.0f / k_scalar(a, b, con.r1, con.r2, n)
            con.tMass = 1.0f / k_scalar(a, b, con.r1, con.r2, cpVect.cpvperp(n))

            // Calculate the target bias velocity.
            val dist = cpVect.cpvdot(cpVect.cpvadd(cpVect.cpvsub(con.r2, con.r1), body_delta), n)
            con.bias = -bias * Math.min(0.0f, dist + slop) / dt
            con.jBias = 0.0f

            // Calculate the target bounce velocity.
            con.bounce = normal_relative_velocity(a, b, con.r1, con.r2, n) * e
        }
    }

    fun cpArbiterApplyCachedImpulse(dt_coef: Float) {
        if (cpArbiterIsFirstContact()) return

        val a = body_a!!
        val b = body_b!!
        val n = n

        contacts.forEach { con ->
            val j = cpVect.cpvrotate(n, cpVect.cpv(con.jnAcc, con.jtAcc))
            apply_impulses(a, b, con.r1, con.r2, cpVect.cpvmult(j, dt_coef))
        }
    }

    fun cpArbiterIsFirstContact(): Boolean {
        return state == cpArbiterState.CP_ARBITER_STATE_FIRST_COLLISION
    }

    fun cpArbiterApplyImpulse() {
        val a = body_a!!
        val b = body_b!!
        val n = n
        val surface_vr = surface_vr
        val friction = u

        for (i in 0 until count) {
            val con = contacts[i]
            val nMass = con.nMass
            val r1 = con.r1
            val r2 = con.r2

            val vb1 = cpVect.cpvadd(a.v_bias, cpVect.cpvmult(cpVect.cpvperp(r1), a.w_bias))
            val vb2 = cpVect.cpvadd(b.v_bias, cpVect.cpvmult(cpVect.cpvperp(r2), b.w_bias))
            val vr = cpVect.cpvadd(relative_velocity(a, b, r1, r2), surface_vr)

            val vbn = cpVect.cpvdot(cpVect.cpvsub(vb2, vb1), n)
            val vrn = cpVect.cpvdot(vr, n)
            val vrt = cpVect.cpvdot(vr, cpVect.cpvperp(n))

            val jbn = (con.bias - vbn) * nMass
            val jbnOld = con.jBias
            con.jBias = Math.max(jbnOld + jbn, 0.0f)

            val jn = -(con.bounce + vrn) * nMass
            val jnOld = con.jnAcc
            con.jnAcc = Math.max(jnOld + jn, 0.0f)

            val jtMax = friction * con.jnAcc
            val jt = -vrt * con.tMass
            val jtOld = con.jtAcc
            con.jtAcc = cpfclamp(jtOld + jt, -jtMax, jtMax);

            apply_bias_impulses(a, b, r1, r2, cpVect.cpvmult(n, con.jBias - jbnOld));
            apply_impulses(a, b, r1, r2, cpVect.cpvrotate(n, cpVect.cpv(con.jnAcc - jnOld, con.jtAcc - jtOld)))
        }
    }
}

/// Clamp @c f to be between @c min and @c max.
fun cpfclamp(f: Float, min: Float, max: Float): Float {
    return Math.min(Math.max(f, min), max)
}

//
//
enum class cpShapeType {
    CP_CIRCLE_SHAPE,
    CP_SEGMENT_SHAPE,
    CP_POLY_SHAPE,
    CP_NUM_SHAPES
}

data class cpMat2x2(
        // Row major [[a, b][c d]]
        var a: Float = 0f,
        var b: Float = 0f,
        var c: Float = 0f,
        var d: Float = 0f
)


fun cpMomentForSegment(m: Float, a: cpVect, b: cpVect, r: Float): Float {
    val offset = cpVect.cpvlerp(a, b, 0.5f)

    // This approximates the shape as a box for rounded segments, but it's quite close.
    val length = cpVect.cpvdist(b, a) + 2.0f * r
    return m * ((length * length + 4.0f * r * r) / 12.0f + cpVect.cpvlengthsq(offset))
}

fun cpMomentForBox(m: Float, width: Float, height: Float): Float {
    return m * (width * width + height * height) / 12.0f
}


fun cpMomentForPoly(m: Float, count: Int, verts: Array<cpVect>, offset: cpVect, r: Float): Float {
    // TODO account for radius.
    if (count == 2) return cpMomentForSegment(m, verts[0], verts[1], 0.0f)

    var sum1 = 0.0f
    var sum2 = 0.0f
    for (i in 0 until count) {
        val v1 = cpVect.cpvadd(verts[i], offset)
        val v2 = cpVect.cpvadd(verts[(i + 1) % count], offset)

        val a = cpVect.cpvcross(v2, v1)
        val b = cpVect.cpvdot(v1, v1) + cpVect.cpvdot(v1, v2) + cpVect.cpvdot(v2, v2)

        sum1 += a * b
        sum2 += a
    }

    return (m * sum1) / (6.0f * sum2)
}

fun cpTransformPoint(t: cpTransform, p: cpVect): cpVect {
    return cpVect.cpv(t.a * p.x + t.c * p.y + t.tx, t.b * p.x + t.d * p.y + t.ty)
}

fun cpClosetPointOnSegment(p: cpVect, a: cpVect, b: cpVect): cpVect {
    val delta = cpVect.cpvsub(a, b)
    val t = cpfclamp01(cpVect.cpvdot(delta, cpVect.cpvsub(p, b)) / cpVect.cpvlengthsq(delta))
    return cpVect.cpvadd(b, cpVect.cpvmult(delta, t))
}


/// Clamp @c f to be between 0 and 1.
fun cpfclamp01(f: Float): Float {
    return Math.max(0.0f, Math.min(f, 1.0f))
}


//
//struct cpSplittingPlane {
//    cpVect v0, n;
//};
//


//
//struct cpPinJoint {
//    cpConstraint constraint;
//    cpVect anchorA, anchorB;
//    cpFloat dist;
//
//    cpVect r1, r2;
//    cpVect n;
//    cpFloat nMass;
//
//    cpFloat jnAcc;
//    cpFloat bias;
//};
//
//struct cpSlideJoint {
//    cpConstraint constraint;
//    cpVect anchorA, anchorB;
//    cpFloat min, max;
//
//    cpVect r1, r2;
//    cpVect n;
//    cpFloat nMass;
//
//    cpFloat jnAcc;
//    cpFloat bias;
//};
//
//struct cpPivotJoint {
//    cpConstraint constraint;
//    cpVect anchorA, anchorB;
//
//    cpVect r1, r2;
//    cpMat2x2 k;
//
//    cpVect jAcc;
//    cpVect bias;
//};
//
//
//struct cpDampedRotarySpring {
//    cpConstraint constraint;
//    cpFloat restAngle;
//    cpFloat stiffness;
//    cpFloat damping;
//    cpDampedRotarySpringTorqueFunc springTorqueFunc;
//
//    cpFloat target_wrn;
//    cpFloat w_coef;
//
//    cpFloat iSum;
//    cpFloat jAcc;
//};
//
//struct cpRotaryLimitJoint {
//    cpConstraint constraint;
//    cpFloat min, max;
//
//    cpFloat iSum;
//
//    cpFloat bias;
//    cpFloat jAcc;
//};
//
//struct cpRatchetJoint {
//    cpConstraint constraint;
//    cpFloat angle, phase, ratchet;
//
//    cpFloat iSum;
//
//    cpFloat bias;
//    cpFloat jAcc;
//};
//
//struct cpGearJoint {
//    cpConstraint constraint;
//    cpFloat phase, ratio;
//    cpFloat ratio_inv;
//
//    cpFloat iSum;
//
//    cpFloat bias;
//    cpFloat jAcc;
//};
//
//struct cpSimpleMotor {
//    cpConstraint constraint;
//    cpFloat rate;
//
//    cpFloat iSum;
//
//    cpFloat jAcc;
//};
//
//typedef struct cpContactBufferHeader cpContactBufferHeader;
//typedef void (*cpSpaceArbiterApplyImpulseFunc)(cpArbiter *arb);
//

//
//typedef struct cpPostStepCallback {
//    cpPostStepFunc func;
//    void *key;
//    void *data;
//} cpPostStepCallback;

data class LoopIndexesresult(var start: Int = 0, var end: Int = 0)

fun cpLoopIndexes(verts: Array<cpVect>, count: Int, res: LoopIndexesresult) {

    var min = verts[0]
    var max = min

    for (i in 1 until count) {
        val v = verts[i]

        if (v.x < min.x || (v.x == min.x && v.y < min.y)) {
            min = v
            res.start = i
        } else if (v.x > max.x || (v.x == max.x && v.y > max.y)) {
            max = v
            res.end = i
        }
    }
}

fun SWAP(A: cpVect, B: cpVect) {
    val TMP = A
    A.x = B.x
    A.y = B.y
    B.x = TMP.x
    B.y = TMP.y
}

fun relative_velocity(a: cpBody, b: cpBody, r1: cpVect, r2: cpVect): cpVect {
    val v1_sum = cpVect.cpvadd(a.v, cpVect.cpvmult(cpVect.cpvperp(r1), a.w))
    val v2_sum = cpVect.cpvadd(b.v, cpVect.cpvmult(cpVect.cpvperp(r2), b.w))

    return cpVect.cpvsub(v2_sum, v1_sum)
}

fun normal_relative_velocity(a: cpBody, b: cpBody, r1: cpVect, r2: cpVect, n: cpVect): Float {
    return cpVect.cpvdot(relative_velocity(a, b, r1, r2), n)
}

fun apply_impulse(body: cpBody, j: cpVect, r: cpVect) {
    body.v = cpVect.cpvadd(body.v, cpVect.cpvmult(j, body.m_inv))
    body.w += body.i_inv * cpVect.cpvcross(r, j)
}

fun apply_impulses(a: cpBody, b: cpBody, r1: cpVect, r2: cpVect, j: cpVect) {
    apply_impulse(a, cpVect.cpvneg(j), r1)
    apply_impulse(b, j, r2)
}

fun apply_bias_impulse(body: cpBody, j: cpVect, r: cpVect) {
    body.v_bias = cpVect.cpvadd(body.v_bias, cpVect.cpvmult(j, body.m_inv))
    body.w_bias += body.i_inv * cpVect.cpvcross(r, j)
}

fun apply_bias_impulses(a: cpBody, b: cpBody, r1: cpVect, r2: cpVect, j: cpVect) {
    apply_bias_impulse(a, cpVect.cpvneg(j), r1)
    apply_bias_impulse(b, j, r2)
}

fun k_scalar_body(body: cpBody, r: cpVect, n: cpVect): Float {
    val rcn = cpVect.cpvcross(r, n)
    return body.m_inv + body.i_inv * rcn * rcn
}

fun k_scalar(a: cpBody, b: cpBody, r1: cpVect, r2: cpVect, n: cpVect): Float {
    val value = k_scalar_body(a, r1, n) + k_scalar_body(b, r2, n)
//    cpAssertSoft(value != 0.0, "Unsolvable collision or constraint.");

    return value
}

fun k_tensor(a: cpBody, b: cpBody, r1: cpVect, r2: cpVect): cpMat2x2 {
    val m_sum = a.m_inv + b.m_inv

    // start with Identity*m_sum
    var k11 = m_sum
    var k12 = 0.0f
    var k21 = 0.0f
    var k22 = m_sum

    // add the influence from r1
    val a_i_inv = a.i_inv
    val r1xsq = r1.x * r1.x * a_i_inv
    val r1ysq = r1.y * r1.y * a_i_inv
    val r1nxy = -r1.x * r1.y * a_i_inv
    k11 += r1ysq
    k12 += r1nxy
    k21 += r1nxy
    k22 += r1xsq

    // add the influnce from r2
    val b_i_inv = b.i_inv
    val r2xsq = r2.x * r2.x * b_i_inv
    val r2ysq = r2.y * r2.y * b_i_inv
    val r2nxy = -r2.x * r2.y * b_i_inv
    k11 += r2ysq
    k12 += r2nxy
    k21 += r2nxy
    k22 += r2xsq

    // invert
    val det = k11 * k22 - k12 * k21
//    cpAssertSoft(det != 0.0, "Unsolvable constraint.");

    val det_inv = 1.0f / det
    return cpMat2x2(k22 * det_inv, -k12 * det_inv, -k21 * det_inv, k11 * det_inv)
}

fun cpMat2x2Transform(m: cpMat2x2, v: cpVect): cpVect {
    return cpVect.cpv(v.x * m.a + v.y * m.b, v.x * m.c + v.y * m.d)
}

fun bias_coef(errorBias: Float, dt: Float): Float {
    return 1.0f - Math.pow(errorBias.toDouble(), dt.toDouble()).toFloat()
}

interface ICPPostStepFunc {
    fun perform(space: cpSpace, key: Any?, data: Any?)
}

data class cpPostStepCallback(
        var func: ICPPostStepFunc?,
        var key: Any?,
        var data: Any?
)