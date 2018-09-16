package chipmunk_bind

data class cpSegmentQueryInfo(var shape: cpShape? = null, var point: cpVect = cpVect.cpvzero, var normal: cpVect = cpVect.cpvzero, var alpha: Float = 0f)

interface ICPShapeClass {
    fun cpShapeType(): cpShapeType
    fun cacheData(transform: cpTransform): cpBB
    fun destroy()
    fun pointQuery(p: cpVect, info: cpPointQueryInfo)
    fun segmentQuery(a: cpVect, b: cpVect, radius: Float, info: cpSegmentQueryInfo)
}

data class cpShapeMassInfo(val m: Float, val i: Float, val cog: cpVect, val area: Float)

abstract class cpShape : cpObject(), ICPShapeClass {

    var space: cpSpace? = null
    lateinit var body: cpBody
    lateinit var massInfo: cpShapeMassInfo
    lateinit var bb: cpBB

    var sensor = false

    var e: Float = 0f
    var u: Float = 0f
    var surfaceV = cpVect()

    var userData: Any? = null //cpDataPointer

    var type = cpCollisionType(0)
    var filter = cpShapeFilter()

    var next: cpShape? = null
    var prev: cpShape? = null

    var hashid: Int = 0

    fun cpShapeInit(shape: cpShape, body: cpBody, massInfo: cpShapeMassInfo) {
        shape.body = body
        shape.massInfo = massInfo

        shape.sensor = false

        shape.e = 0.0f
        shape.u = 0.0f
        shape.surfaceV = cpVect.cpvzero

        body.shapeList.add(shape)
        shape.type = cpCollisionType(0)
        shape.filter.group = cpGroup.CP_NO_GROUP
        shape.filter.categories = cpBitmask.CP_ALL_CATEGORIES
        shape.filter.mask = cpBitmask.CP_ALL_CATEGORIES
    }

    fun cpShapeSetFriction(friction: Float) {
        AssertionError(body != null)
        AssertionError(friction >= 0.0f)
        body.cpBodyActivate()
        u = friction
    }

    fun cpShapeGetFriction() = u

    fun cpShapeGetElasticity() = e

    fun cpShapeSetElasticity(elasticity: Float) {
        AssertionError(body != null)
        AssertionError(elasticity >= 0.0f)
        body.cpBodyActivate()
        e = elasticity
    }

    fun cpShapeSetFilter(filter: cpShapeFilter) {
        body.cpBodyActivate()
        this.filter = filter
    }


    fun cpShapeGetCollisionType(): cpCollisionType {
        return type
    }

    fun cpShapeSetCollisionType(collisionType: cpCollisionType) {
        body?.cpBodyActivate()
        type = collisionType
    }

    fun cpShapeGetCenterOfGravity(): cpVect = massInfo?.cog ?: cpVect.cpvzero

    fun cpShapeUpdate(transform: cpTransform): cpBB {
        bb = cacheData(transform)
        return bb
    }
};

class cpCircleShape(body: cpBody, val radius: Float, val offset: cpVect = cpVect(0f, 0f)) : cpShape() {
//    var shape: cpShape? = null

    var c: cpVect = cpVect.cpvzero
    var tc: cpVect = cpVect.cpvzero
    var r: Float = 0f

    init {

//    """body is the body attach the circle to, offset is the offset from the
//        body's center of gravity in body local coordinates.
//
//        It is legal to send in None as body argument to indicate that this
//        shape is not attached to a body. However, you must attach it to a body
//        before adding the shape to a space or used for a space shape query.
//        """

        c = offset
        r = radius

        cpShapeInit(this, body, cpCircleShapeMassInfo(0.0f, radius, offset))

    }

    override fun cpShapeType(): cpShapeType = cpShapeType.CP_CIRCLE_SHAPE

    override fun cacheData(transform: cpTransform): cpBB {
        val c = cpTransformPoint(transform, c)
        tc = cpTransformPoint(transform, c)
        return cpBBNewForCircle(c, r)
    }

    override fun destroy() {}
    override fun pointQuery(p: cpVect, info: cpPointQueryInfo) {
        val delta = cpVect.cpvsub(p, tc)
        val d = cpVect.cpvlength(delta)
        val r = r

        info.shape = this
        info.point = cpVect.cpvadd(tc, cpVect.cpvmult(delta, r / d))
        info.distance = d - r

        // Use up for the gradient if the distance is very small.
        info.gradient = if (d > MAGIC_EPSILON) cpVect.cpvmult(delta, 1.0f / d) else cpVect.cpv(0.0f, 1.0f)
    }

    override fun segmentQuery(a: cpVect, b: cpVect, radius: Float, info: cpSegmentQueryInfo) {
        CircleSegmentQuery(this, tc, r, a, b, radius, info)
    }
}

fun CircleSegmentQuery(shape: cpShape, center: cpVect, r1: Float, a: cpVect, b: cpVect, r2: Float, info: cpSegmentQueryInfo) {
    val da = cpVect.cpvsub(a, center)
    val db = cpVect.cpvsub(b, center)
    val rsum = r1 + r2

    val qa = cpVect.cpvdot(da, da) - 2.0f * cpVect.cpvdot(da, db) + cpVect.cpvdot(db, db)
    val qb = cpVect.cpvdot(da, db) - cpVect.cpvdot(da, da)
    val det = qb * qb - qa * (cpVect.cpvdot(da, da) - rsum * rsum)

    if (det >= 0.0f) {
        val t = (-qb - Math.sqrt(det.toDouble()).toFloat()) / (qa)
        if (0.0f <= t && t <= 1.0f) {
            val n = cpVect.cpvnormalize(cpVect.cpvlerp(da, db, t))

            info.shape = shape
            info.point = cpVect.cpvsub(cpVect.cpvlerp(a, b, t), cpVect.cpvmult(n, r2))
            info.normal = n
            info.alpha = t
        }
    }
}

class cpSegmentShape : cpShape() {
//    var shape: cpShape? = null

    var a = cpVect()
    var b = cpVect()
    var n = cpVect()
    var ta = cpVect()
    var tb = cpVect()
    var tn = cpVect()
    var r: Float = 0f

    var a_tangent = cpVect()
    var b_tangent = cpVect()

    fun init(body: cpBody, a: cpVect, b: cpVect, rad: Float) {
        this.a = a
        this.b = b
        this.r = rad
        this.n = cpVect.cpvrperp(cpVect.cpvnormalize(cpVect.cpvsub(b, a)))
        this.a_tangent = cpVect.cpvzero
        this.b_tangent = cpVect.cpvzero


        cpShapeInit(this, body, cpSegmentShapeMassInfo(0.0f, a, b, r))
    }

    override fun cacheData(transform: cpTransform): cpBB {

        ta = cpTransformPoint(transform, a)
        tb = cpTransformPoint(transform, b)
        tn = cpTransformVect(transform, n)

        var l = 0f
        var r = 0f
        var b = 0f
        var t = 0f

        if (ta.x < tb.x) {
            l = ta.x
            r = tb.x
        } else {
            l = tb.x
            r = ta.x
        }

        if (ta.y < tb.y) {
            b = ta.y
            t = tb.y
        } else {
            b = tb.y
            t = ta.y
        }

        val rad = this.r
        return cpBB(l - rad, b - rad, r + rad, t + rad)

    }

    override fun cpShapeType(): cpShapeType = cpShapeType.CP_SEGMENT_SHAPE

    override fun destroy() {}

    override fun pointQuery(p: cpVect, info: cpPointQueryInfo) {

        val closest = cpClosetPointOnSegment(p, ta, tb)

        val delta = cpVect.cpvsub(p, closest)
        val d = cpVect.cpvlength(delta)
        val r = this.r
        val g = cpVect.cpvmult(delta, 1.0f / d)

        info.shape = this
        info.point = if (d != 0f) cpVect.cpvadd(closest, cpVect.cpvmult(g, r)) else closest
        info.distance = d - r

        // Use the segment's normal if the distance is very small.
        info.gradient = if (d > MAGIC_EPSILON) g else n

    }

    override fun segmentQuery(a: cpVect, b: cpVect, radius: Float, info: cpSegmentQueryInfo) {
        val n = tn
        val d = cpVect.cpvdot(cpVect.cpvsub(ta, a), n)
        val r = r + radius

        val flipped_n = if (d > 0.0f) cpVect.cpvneg(n) else n
        val seg_offset = cpVect.cpvsub(cpVect.cpvmult(flipped_n, r), a)

        // Make the endpoints relative to 'a' and move them by the thickness of the segment.
        val seg_a = cpVect.cpvadd(ta, seg_offset)
        val seg_b = cpVect.cpvadd(tb, seg_offset)
        val delta = cpVect.cpvsub(b, a)

        if (cpVect.cpvcross(delta, seg_a) * cpVect.cpvcross(delta, seg_b) <= 0.0f) {
            val d_offset = d + if (d > 0.0f) -r else r
            val ad = -d_offset
            val bd = cpVect.cpvdot(delta, n) - d_offset

            if (ad * bd < 0.0f) {
                val t = ad / (ad - bd)

                info.shape = this
                info.point = cpVect.cpvsub(cpVect.cpvlerp(a, b, t), cpVect.cpvmult(flipped_n, radius))
                info.normal = flipped_n
                info.alpha = t
            }
        } else if (r != 0.0f) {
            val info1 = cpSegmentQueryInfo(null, b, cpVect.cpvzero, 1.0f)
            val info2 = cpSegmentQueryInfo(null, b, cpVect.cpvzero, 1.0f)
            CircleSegmentQuery(this, ta, this.r, a, b, radius, info1)
            CircleSegmentQuery(this, tb, this.r, a, b, radius, info2)

            if (info1.alpha < info2.alpha) {
                info.shape = info1.shape
                info.point = info1.point
                info.normal = info1.normal
                info.alpha = info1.alpha
            } else {
                info.shape = info2.shape
                info.point = info2.point
                info.normal = info2.normal
                info.alpha = info2.alpha
            }
        }
    }

}


const val CP_POLY_SHAPE_INLINE_ALLOC = 6

data class cpSplittingPlane(var v0: cpVect, var n: cpVect)

class cpPolyShape : cpShape() {
    //#define CP_POLY_SHAPE_INLINE_ALLOC 6

    //    cpShape shape;
//
    var r: Float = 0f
    //
    var count: Int = 0

    //    // The untransformed planes are appended at the end of the transformed planes.
    val planes = ArrayList<cpSplittingPlane>()
    //
//    // Allocate a small number of splitting planes internally for simple poly.
    val _planes = Array<cpSplittingPlane>(2 * CP_POLY_SHAPE_INLINE_ALLOC) { i -> cpSplittingPlane(cpVect.cpvzero, cpVect.cpvzero) }


    //
    fun init(body: cpBody, vertices: Array<cpVect>, transform: cpTransform? = null, radius: Float = 0f) {
//    """Create a polygon.
//
//        A convex hull will be calculated from the vertexes automatically.
//
//        Adding a small radius will bevel the corners and can significantly
//        reduce problems where the poly gets stuck on seams in your geometry.
//
//        It is legal to send in None as body argument to indicate that this
//        shape is not attached to a body. However, you must attach it to a body
//        before adding the shape to a space or used for a space shape query.
//
//        :param Body body: The body to attach the poly to
//        :param [(float,float)] vertices: Define a convex hull of the polygon
//            with a counterclockwise winding.
//        :param Transform transform: Transform will be applied to every vertex.
//        :param float radius: Set the radius of the poly shape
//
//        """

        val trans = transform ?: cpTransform.identity()
//
//        vs = list(map(tuple, vertices))
//
//        s = cp.cpPolyShapeNew(body_body, len(vertices), vs, transform, radius)
//        self._shape = ffi.gc(s, cp.cpShapeFree)
//        self._set_shapeid()


//        cpPolyShape *
//                cpPolyShapeInit(cpPolyShape *poly, cpBody *body, int count, const cpVect *verts, cpTransform transform, cpFloat radius)
//                {
        val hullVerts = Array<cpVect>(vertices.size) { i -> cpTransformPoint(trans, vertices[i]) }
//                    *hullVerts = (cpVect *)alloca(count*sizeof(cpVect));

        // Transform the verts before building the hull in case of a negative scale.
//                    for(int i=0; i<count; i++) hullVerts[i] = cpTransformPoint(transform, verts[i]);

        val hullCount = cpConvexHull(vertices.size, hullVerts, hullVerts, null, 0.0f)

        cpShapeInit(this, body, cpPolyShapeMassInfo(0.0f, hullCount, hullVerts, radius))

        SetVerts(hullCount, hullVerts)
        r = radius
    }

    fun SetVerts(h_count: Int, verts: Array<cpVect>) {
        count = h_count
        if (count <= CP_POLY_SHAPE_INLINE_ALLOC) {
            planes.clear()
            planes.addAll(_planes)
        }

        for (i in 0 until count) {
            val a = verts[(i - 1 + count) % count]
            val b = verts[i]
            val n = cpVect.cpvnormalize(cpVect.cpvrperp(cpVect.cpvsub(b, a)))

            planes[i + count].v0 = b
            planes[i + count].n = n
        }
    }

    override fun cacheData(transform: cpTransform): cpBB {

        val count = count
        val dst = planes
        val src = dst.subList(count, dst.size)

        var l = INFINITY
        var r = -INFINITY
        var b = INFINITY
        var t = -INFINITY

        for (i in 0 until count) {
            val v1 = cpTransformPoint(transform, src[i].v0)
            val n1 = cpTransformVect(transform, src[i].n)

            dst[i].v0 = v1
            dst[i].n = n1

            l = Math.min(l, v1.x)
            r = Math.max(r, v1.x)
            b = Math.min(b, v1.y)
            t = Math.max(t, v1.y)
        }

        val radius = this.r
        bb = cpBB(l - radius, b - radius, r + radius, t + radius)
        return bb

    }

    override fun cpShapeType(): cpShapeType = cpShapeType.CP_POLY_SHAPE

    override fun destroy() {

    }

    override fun pointQuery(p: cpVect, info: cpPointQueryInfo) {
        val count = count;
        val planes = planes;
        val r = r

        var v0 = planes[count - 1].v0
        var minDist = INFINITY
        var closestPoint = cpVect.cpvzero
        var closestNormal = cpVect.cpvzero
        var outside = false

        for (i in 0 until count) {
            val v1 = planes[i].v0
            outside = outside || (cpVect.cpvdot(planes[i].n, cpVect.cpvsub(p, v1)) > 0.0f)

            val closest = cpClosetPointOnSegment(p, v0, v1)

            val dist = cpVect.cpvdist(p, closest)
            if (dist < minDist) {
                minDist = dist
                closestPoint = closest
                closestNormal = planes[i].n
            }

            v0 = v1
        }

        val dist = if (outside) minDist else -minDist
        val g = cpVect.cpvmult(cpVect.cpvsub(p, closestPoint), 1.0f / dist)

        info.shape = this
        info.point = cpVect.cpvadd(closestPoint, cpVect.cpvmult(g, r))
        info.distance = dist - r

        // Use the normal of the closest segment if the distance is small.
        info.gradient = if (minDist > MAGIC_EPSILON) g else closestNormal

    }

    override fun segmentQuery(a: cpVect, b: cpVect, radius: Float, info: cpSegmentQueryInfo) {
        val planes = planes
        val count = count
        val r = r
        val rsum = r + radius

        for (i in 0 until count) {
            val n = planes[i].n
            val an = cpVect.cpvdot(a, n)
            val d = an - cpVect.cpvdot(planes[i].v0, n) - rsum
            if (d < 0.0f) continue

            val bn = cpVect.cpvdot(b, n)
            val t = d / (an - bn)
            if (t < 0.0f || 1.0f < t) continue

            val point = cpVect.cpvlerp(a, b, t)
            val dt = cpVect.cpvcross(n, point)
            val dtMin = cpVect.cpvcross(n, planes[(i - 1 + count) % count].v0);
            val dtMax = cpVect.cpvcross(n, planes[i].v0);

            if (dtMin <= dt && dt <= dtMax) {
                info.shape = this
                info.point = cpVect.cpvsub(cpVect.cpvlerp(a, b, t), cpVect.cpvmult(n, radius))
                info.normal = n
                info.alpha = t
            }
        }

        // Also check against the beveled vertexes.
        if (rsum > 0.0f) {
            for (i in 0 until count) {
                val circle_info = cpSegmentQueryInfo(null, b, cpVect.cpvzero, 1.0f)
                CircleSegmentQuery(this, planes[i].v0, r, a, b, radius, circle_info)
                if (circle_info.alpha < info.alpha) {


                    info.shape = circle_info.shape
                    info.point = circle_info.point
                    info.normal = circle_info.normal
                    info.alpha = circle_info.alpha
                }
            }
        }
    }


}


fun QHullPartition(verts: Array<cpVect>, count: Int, a: cpVect, b: cpVect, tol: Float): Int {
    if (count == 0) return 0

    var max: Float = 0f
    var pivot: Int = 0

    val delta = cpVect.cpvsub(b, a)
    val valueTol: Float = tol * cpVect.cpvlength(delta)

    var head = 0
    var tail = count - 1
    while (head <= tail) {
        val value = cpVect.cpvcross(cpVect.cpvsub(verts[head], a), delta)
        if (value > valueTol) {
            if (value > max) {
                max = value
                pivot = head
            }

            head++
        } else {
            SWAP(verts[head], verts[tail])
            tail--
        }
    }

    // move the new pivot to the front if it's not already there.
    if (pivot != 0) SWAP(verts[0], verts[pivot])
    return head
}


fun QHullReduce(tol: Float, verts: Array<cpVect>, count: Int, a: cpVect, pivot: cpVect, b: cpVect, result: Array<cpVect>): Int {
    return if (count < 0) {
        0
    } else if (count == 0) {
        result[0] = pivot
        1
    } else {
        val left_count = QHullPartition(verts, count, a, pivot, tol)
        var index = QHullReduce(tol, verts.copyOfRange(1, count), left_count - 1, a, verts[0], pivot, result)

        result[index++] = pivot

        val right_count = QHullPartition(verts.copyOfRange(left_count, count), count - left_count, pivot, b, tol)
        index + QHullReduce(tol, verts.copyOfRange(left_count + 1, count), right_count - 1, pivot, verts[left_count], b, result.copyOfRange(index, count))
    }
}

data class convexHullResult(var first: Int = 0)

fun cpConvexHull(count: Int, verts: Array<cpVect>, result: Array<cpVect>, first: convexHullResult? = null, tol: Float): Int {
//    if (verts != result) {
//        // Copy the line vertexes into the empty part of the result polyline to use as a scratch buffer.
//        memcpy(result, verts, count * sizeof(cpVect))
//    }

    // Degenerate case, all points are the same.
    val loop_result = LoopIndexesresult()

    cpLoopIndexes(verts, count, loop_result)
    if (loop_result.start == loop_result.end) {
        if (first != null) first.first = 0
        return 1
    }

    SWAP(result[0], result[loop_result.start])
    SWAP(result[1], result[if (loop_result.end == 0) loop_result.start else loop_result.end])

    val a: cpVect = result[0]
    val b: cpVect = result[1]

    if (first != null) first.first = loop_result.start
    return QHullReduce(tol, result.copyOfRange(2, count), count - 2, a, b, a, result.copyOfRange(1, count)) + 1
}

/// Fast collision filtering type that is used to determine if two objects collide before calling collision or query callbacks.
class cpShapeFilter(var group: cpGroup = cpGroup.CP_NO_GROUP) {
    /// Two objects with the same non-zero group value do not collide.
    /// This is generally used to group objects in a composite object together to disable self collisions.

    /// A bitmask of user definable categories that this object belongs to.
    /// The category/mask combinations of both objects in a collision must agree for a collision to occur.
    var categories: cpBitmask = cpBitmask.CP_ALL_CATEGORIES
    /// A bitmask of user definable category types that this object object collides with.
    /// The category/mask combinations of both objects in a collision must agree for a collision to occur.
    var mask: cpBitmask = cpBitmask.CP_ALL_CATEGORIES
}

fun cpAreaForSegment(a: cpVect, b: cpVect, r: Float): Float {
    return r * (CP_PI * r + 2.0f * cpVect.cpvdist(a, b))
}

fun cpSegmentShapeMassInfo(mass: Float, a: cpVect, b: cpVect, r: Float): cpShapeMassInfo {
    return cpShapeMassInfo(mass,
            cpMomentForBox(1.0f, cpVect.cpvdist(a, b) + 2.0f * r, 2.0f * r),
            cpVect.cpvlerp(a, b, 0.5f),
            cpAreaForSegment(a, b, r))
}

fun cpCircleShapeMassInfo(mass: Float, radius: Float, center: cpVect): cpShapeMassInfo {
    return cpShapeMassInfo(mass, cpMomentForCircle(1.0f, 0.0f, radius, cpVect.cpvzero), center, cpAreaForCircle(0.0f, radius))
}

fun cpAreaForCircle(r1: Float, r2: Float): Float {
    return CP_PI * Math.abs(r1 * r1 - r2 * r2)
}


fun cpCentroidForPoly(count: Int, verts: Array<cpVect>): cpVect {
    var sum = 0.0f
    var vsum = cpVect.cpvzero

    for (i in 0 until count) {
        val v1 = verts[i]
        val v2 = verts[(i + 1) % count]
        val cross = cpVect.cpvcross(v1, v2)

        sum += cross
        vsum = cpVect.cpvadd(vsum, cpVect.cpvmult(cpVect.cpvadd(v1, v2), cross))
    }

    return cpVect.cpvmult(vsum, 1.0f / (3.0f * sum))
}


fun cpPolyShapeMassInfo(mass: Float, count: Int, verts: Array<cpVect>, radius: Float): cpShapeMassInfo {
    // TODO moment is approximate due to radius.

    val centroid = cpCentroidForPoly(count, verts)

    return cpShapeMassInfo(mass,
            cpMomentForPoly(1.0f, count, verts, cpVect.cpvneg(centroid), radius),
            centroid,
            cpAreaForPoly(count, verts, radius))

}


fun cpAreaForPoly(count: Int, verts: Array<cpVect>, r: Float): Float {
    var area = 0.0f
    var perimeter = 0.0f
    for (i in 0 until count) {
        val v1 = verts[i]
        val v2 = verts[(i + 1) % count]

        area += cpVect.cpvcross(v1, v2)
        perimeter += cpVect.cpvdist(v1, v2)
    }

    return r * (CP_PI * Math.abs(r) + perimeter) + area / 2.0f
}