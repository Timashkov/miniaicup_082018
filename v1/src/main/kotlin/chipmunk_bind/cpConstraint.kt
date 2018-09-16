package chipmunk_bind

interface ICPConstraintClass {
    fun preStep(constraint: cpConstraint, dt: Float)
    fun applyCachedImpulse(constraint: cpConstraint, dt_coef: Float)
    fun applyImpulse(constraint: cpConstraint, dt: Float)
    fun getImpulse(constraint: cpConstraint): Float
}

/// Callback function type that gets called before solving a joint.
interface ICPConstraintPreSolveFunc {
    fun perform(constraint: cpConstraint, space: cpSpace)
}

/// Callback function type that gets called after solving a joint.
interface ICPConstraintPostSolveFunc {
    fun perform(constraint: cpConstraint, space: cpSpace)
}

open class cpConstraint(var a: cpBody, var b: cpBody) : cpObject() {
    lateinit var klass: ICPConstraintClass

    var space: cpSpace? = null

    var maxForce: Float = Float.POSITIVE_INFINITY
    var errorBias: Float = Math.pow((1.0f - 0.1f).toDouble(), 60.0).toFloat()
    var maxBias: Float = Float.POSITIVE_INFINITY

    var collideBodies = true

    var preSolve: ICPConstraintPreSolveFunc? = null
    var postSolve: ICPConstraintPostSolveFunc? = null

    var userData: Any? = null

    fun cpConstraintInit(constraint: cpConstraint, klass: ICPConstraintClass) {
        constraint.klass = klass
    }
}

class cpGrooveJoint(a: cpBody, b: cpBody, val grv_a: cpVect, val grv_b: cpVect, val anchorB: cpVect) : cpConstraint(a, b) {

    var grv_n: cpVect = cpVect.cpvzero

    var grv_tn: cpVect = cpVect.cpvzero
    var clamp: Float = 0f
    var r1: cpVect = cpVect.cpvzero
    var r2: cpVect = cpVect.cpvzero
    var k: cpMat2x2 = cpMat2x2()

    var jAcc: cpVect = cpVect.cpvzero
    var bias: cpVect = cpVect.cpvzero

    init {
//        """The groove goes from groove_a to groove_b on body a, and the pivot
//        is attached to anchor_b on body b.
//
//        All coordinates are body local.
//        """
        cpConstraintInit(this, object : ICPConstraintClass {
            override fun preStep(constraint: cpConstraint, dt: Float) {
                val joint = constraint as cpGrooveJoint
                val a = joint.a
                val b = joint.b

                // calculate endpoints in worldspace
                val ta = cpTransformPoint(a.transform, joint.grv_a)
                val tb = cpTransformPoint(a.transform, joint.grv_b)

                // calculate axis
                val n = cpTransformVect(a.transform, joint.grv_n)
                val d = cpVect.cpvdot(ta, n)

                joint.grv_tn = n
                joint.r2 = cpTransformVect(b.transform, cpVect.cpvsub(joint.anchorB, b.cog))

                // calculate tangential distance along the axis of r2
                val td = cpVect.cpvcross(cpVect.cpvadd(b.p, joint.r2), n)
                // calculate clamping factor and r2
                if (td <= cpVect.cpvcross(ta, n)) {
                    joint.clamp = 1.0f
                    joint.r1 = cpVect.cpvsub(ta, a.p)
                } else if (td >= cpVect.cpvcross(tb, n)) {
                    joint.clamp = -1.0f
                    joint.r1 = cpVect.cpvsub(tb, a.p)
                } else {
                    joint.clamp = 0.0f
                    joint.r1 = cpVect.cpvsub(cpVect.cpvadd(cpVect.cpvmult(cpVect.cpvperp(n), -td), cpVect.cpvmult(n, d)), a.p)
                }

                // Calculate mass tensor
                joint.k = k_tensor(a, b, joint.r1, joint.r2)

                // calculate bias velocity
                val delta = cpVect.cpvsub(cpVect.cpvadd(b.p, joint.r2), cpVect.cpvadd(a.p, joint.r1))
                joint.bias = cpVect.cpvclamp(cpVect.cpvmult(delta, -bias_coef(joint.errorBias, dt) / dt), joint.maxBias)
            }

            override fun applyCachedImpulse(constraint: cpConstraint, dt_coef: Float) {
                val joint = constraint as cpGrooveJoint
                val a = joint.a
                val b = joint.b

                apply_impulses(a, b, joint.r1, joint.r2, cpVect.cpvmult(joint.jAcc, dt_coef))
            }

            override fun applyImpulse(constraint: cpConstraint, dt: Float) {
                val joint = constraint as cpGrooveJoint
                val a = joint.a
                val b = joint.b

                val r1 = joint.r1
                val r2 = joint.r2

                // compute impulse
                val vr = relative_velocity(a, b, r1, r2)

                var j = cpMat2x2Transform(joint.k, cpVect.cpvsub(joint.bias, vr))
                val jOld = joint.jAcc
                joint.jAcc = grooveConstrain(joint, cpVect.cpvadd(jOld, j), dt)
                j = cpVect.cpvsub(joint.jAcc, jOld)

                // apply impulse
                apply_impulses(a, b, joint.r1, joint.r2, j)
            }

            override fun getImpulse(constraint: cpConstraint): Float {
                return cpVect.cpvlength((constraint as cpGrooveJoint).jAcc)
            }

        })

        grv_n = cpVect.cpvperp(cpVect.cpvnormalize(cpVect.cpvsub(grv_b, grv_a)))
    }
}

fun grooveConstrain(joint: cpGrooveJoint, j: cpVect, dt: Float): cpVect {
    val n = joint.grv_tn
    val jClamp = if (joint.clamp * cpVect.cpvcross(j, n) > 0.0f) j else cpVect.cpvproject(j, n)
    return cpVect.cpvclamp(jClamp, joint.maxForce * dt)
}

interface IcpDampedSpringForceFunc {
    fun run(spring: cpDampedSpring, dist: Float): Float
}

class cpDampedSpring(a: cpBody, b: cpBody, var anchorA: cpVect, var anchorB: cpVect, var restLength: Float, var stiffness: Float, var damping: Float) : cpConstraint(a, b) {
//    cpConstraint constraint;

    var dampingFloat = 0f
    var springForceFunc: IcpDampedSpringForceFunc

    var target_vrn: Float = 0f
    var v_coef: Float = 0f

    var r1: cpVect = cpVect.cpvzero
    var r2: cpVect = cpVect.cpvzero
    var nMass: Float = 0f
    var n: cpVect = cpVect.cpvzero

    var jAcc: Float = 0f

    init {
//        """Defined much like a slide joint.
//
//        :param Body a: Body a
//        :param Body b: Body b
//        :param anchor_a: Anchor point a, relative to body a
//        :type anchor_a: `(float,float)`
//        :param anchor_b: Anchor point b, relative to body b
//        :type anchor_b: `(float,float)`
//        :param float rest_length: The distance the spring wants to be.
//        :param float stiffness: The spring constant (Young's modulus).
//        :param float damping: How soft to make the damping of the spring.
//        """

        cpConstraintInit(this, object : ICPConstraintClass {
            override fun preStep(constraint: cpConstraint, dt: Float) {
                val spring = constraint as cpDampedSpring

                val a = spring.a
                val b = spring.b

                spring.r1 = cpTransformVect(a.transform, cpVect.cpvsub(spring.anchorA, a.cog))
                spring.r2 = cpTransformVect(b.transform, cpVect.cpvsub(spring.anchorB, b.cog))

                val delta = cpVect.cpvsub(cpVect.cpvadd(b.p, spring.r2), cpVect.cpvadd(a.p, spring.r1))
                val dist = cpVect.cpvlength(delta)
                spring.n = cpVect.cpvmult(delta, 1.0f / (if (dist != 0f) dist else INFINITY))

                val k = k_scalar(a, b, spring.r1, spring.r2, spring.n)
//                cpAssertSoft(k != 0.0, "Unsolvable spring.");
                spring.nMass = 1.0f / k

                spring.target_vrn = 0.0f
                spring.v_coef = 1.0f - Math.exp((-spring.damping * dt * k).toDouble()).toFloat()

                // apply spring force
                val f_spring = spring.springForceFunc.run(spring, dist)
                val j_spring = f_spring * dt
                spring.jAcc = f_spring * dt
                apply_impulses(a, b, spring.r1, spring.r2, cpVect.cpvmult(spring.n, j_spring))
            }

            override fun applyCachedImpulse(constraint: cpConstraint, dt_coef: Float) {
            }

            override fun applyImpulse(constraint: cpConstraint, dt: Float) {
                val spring = constraint as cpDampedSpring

                val a = spring.a
                val b = spring.b

                val n = spring.n
                val r1 = spring.r1
                val r2 = spring.r2

                // compute relative velocity
                val vrn = normal_relative_velocity(a, b, r1, r2, n)

                // compute velocity loss from drag
                val v_damp = (spring.target_vrn - vrn) * spring.v_coef
                spring.target_vrn = vrn + v_damp

                val j_damp = v_damp * spring.nMass
                spring.jAcc += j_damp
                apply_impulses(a, b, spring.r1, spring.r2, cpVect.cpvmult(spring.n, j_damp))
            }

            override fun getImpulse(constraint: cpConstraint): Float {
                val spring = constraint as cpDampedSpring
                return spring.jAcc
            }

        })

        springForceFunc = object : IcpDampedSpringForceFunc {
            //default func
            override fun run(spring: cpDampedSpring, dist: Float): Float {
                return (spring.restLength - dist) * spring.stiffness
            }
        }

    }


}

class cpSimpleMotor(a: cpBody, b: cpBody, var rate: Float) : cpConstraint(a, b) {

    var iSum: Float = 0f
    var jAcc: Float = 0f

    init {
        cpConstraintInit(this, object : ICPConstraintClass {
            override fun preStep(constraint: cpConstraint, dt: Float) {
                val joint = constraint as cpSimpleMotor
                val a = joint.a
                val b = joint.b
                // calculate moment of inertia coefficient.
                joint.iSum = 1.0f / (a.i_inv + b.i_inv)
            }

            override fun applyCachedImpulse(constraint: cpConstraint, dt_coef: Float) {
                val joint = constraint as cpSimpleMotor

                val a = joint.a
                val b = joint.b

                val j = joint.jAcc * dt_coef
                a.w -= j * a.i_inv
                b.w += j * b.i_inv
            }

            override fun applyImpulse(constraint: cpConstraint, dt: Float) {
                val joint = constraint as cpSimpleMotor

                val a = joint.a
                val b = joint.b

                // compute relative rotational velocity
                val wr = b.w - a.w + joint.rate

                val jMax = joint.maxForce * dt

                // compute normal impulse
                var j = -wr * joint.iSum
                val jOld = joint.jAcc
                joint.jAcc = cpfclamp(jOld + j, -jMax, jMax)
                j = joint.jAcc - jOld;

                // apply impulse
                a.w -= j * a.i_inv
                b.w += j * b.i_inv
            }

            override fun getImpulse(constraint: cpConstraint): Float {
                val joint = constraint as cpSimpleMotor
                return Math.abs(joint.jAcc)
            }

        })
    }
}