package chipmunk_bind

class cpVect() {
    constructor(x: Float, y: Float) : this() {
        this.x = x
        this.y = y
    }

    var x: Float = 0.0f
    var y: Float = 0.0f


    companion object {
        val cpvzero = cpVect(0f, 0f)
        fun cpv(x: Float, y: Float): cpVect = cpVect(x, y)
        fun cpvrperp(v: cpVect): cpVect = cpVect(v.y, -v.x)
        fun cpvnormalize(v: cpVect): cpVect = cpvmult(v, 1.0f / (cpvlength(v) + Float.MIN_VALUE))

        fun cpvsub(v1: cpVect, v2: cpVect): cpVect = cpVect(v1.x - v2.x, v1.y - v2.y)
        fun cpvmult(v: cpVect, s: Float): cpVect = cpVect(v.x * s, v.y * s)

        fun cpvlength(v: cpVect): Float = cpfsqrt(cpvdot(v, v))

        fun cpvdot(v1: cpVect, v2: cpVect): Float = v1.x * v2.x + v1.y * v2.y

        fun cpfsqrt(f: Float): Float = Math.sqrt(f.toDouble()).toFloat()

        /// Returns the distance between v1 and v2.
        fun cpvdist(v1: cpVect, v2: cpVect): Float = cpvlength(cpvsub(v1, v2))

        /// Linearly interpolate between v1 and v2.
        fun cpvlerp(v1: cpVect, v2: cpVect, t: Float): cpVect = cpvadd(cpvmult(v1, 1.0f - t), cpvmult(v2, t))

        /// Add two vectors
        fun cpvadd(v1: cpVect, v2: cpVect): cpVect = cpv(v1.x + v2.x, v1.y + v2.y)

        /// Returns the unit length vector for the given angle (in radians).
        fun cpvforangle(a: Float): cpVect = cpv(Math.cos(a.toDouble()).toFloat(), Math.sin(a.toDouble()).toFloat())

        /// Returns the squared length of v. Faster than cpvlength() when you only need to compare lengths.
        fun cpvlengthsq(v: cpVect): Float = cpvdot(v, v)

        /// Returns a perpendicular vector. (90 degree rotation)
        fun cpvperp(v: cpVect): cpVect = cpv(-v.y, v.x)

        fun cpvcross(v1: cpVect, v2: cpVect): Float = v1.x * v2.y - v1.y * v2.x

        /// Negate a vector.
        fun cpvneg(v: cpVect): cpVect = cpv(-v.x, -v.y)

        fun cpvdistsq(v1: cpVect, v2: cpVect): Float = cpvlengthsq(cpvsub(v1, v2))

        /// Clamp v to length len.
        fun cpvclamp(v: cpVect, len: Float): cpVect {
            return if (cpvdot(v, v) > len * len) cpvmult(cpvnormalize(v), len) else v
        }

        /// Returns the vector projection of v1 onto v2.
        fun cpvproject(v1: cpVect, v2: cpVect): cpVect {
            return cpvmult(v2, cpvdot(v1, v2) / cpvdot(v2, v2))
        }

        /// Uses complex number multiplication to rotate v1 by v2. Scaling will occur if v1 is not a unit vector.
        fun cpvrotate(v1: cpVect, v2: cpVect): cpVect {
            return cpv(v1.x * v2.x - v1.y * v2.y, v1.x * v2.y + v1.y * v2.x)
        }
    }
}


