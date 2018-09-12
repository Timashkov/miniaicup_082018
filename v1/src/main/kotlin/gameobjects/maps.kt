package gameobjects

import chipmunk_bind.cpSegmentShape
import chipmunk_bind.cpSpace
import chipmunk_bind.cpVect

data class Arc(val c: cpVect, val r: Float, val a: Float, val b: Float, val sc: Int)
data class Segment(val fp: cpVect, val sp: cpVect, var heigth: Float = 0f)

abstract class Map(val space: cpSpace) {
    var external_id = 0

    var segment_friction = 1f
    var segment_elasticity = 0f
    var segment_height = 10f

//    var cars_start_position = []
//
    var max_width = 1200f
    var max_height = 800f
    val objects = ArrayList<cpSegmentShape>()

    abstract fun getBaseArcs(): Array<Arc>
    abstract fun getAdditionalArcs(): Array<Arc>
    abstract fun getBaseSegments(): Array<Segment>
    abstract fun getAdditionalSegments(): Array<Segment>

    init {

        create_box(space)

        val sps = _get_segments_points()
        sps.forEach { it ->
            val segment = cpSegmentShape()
            segment.init(space.staticBody, it.fp, it.sp, it.heigth)
            segment.cpShapeSetFriction(segment_friction)
            segment.cpShapeSetElasticity(segment_elasticity)
            objects.add(segment)
        }

    }

    fun create_box(space: cpSpace) {
        val bo = segment_height - 1  // box offset
        val left = cpSegmentShape()
        left.init(space.staticBody, cpVect(-bo, -bo), cpVect(-bo, max_height + bo), segment_height)
        left.sensor = true
        objects.add(left)

        val top = cpSegmentShape()
        top.init(space.staticBody, cpVect(-bo, max_height + bo),
                cpVect(max_width + bo, max_height + bo), segment_height)
        top.sensor = true
        objects.add(top)

        val right = cpSegmentShape()
        right.init(space.staticBody, cpVect(max_width + bo, max_height + bo),
                cpVect(max_width + bo, -bo), segment_height)
        right.sensor = true
        objects.add(right)

        val bottom = cpSegmentShape()
        bottom.init(space.staticBody, cpVect(max_width + bo, -bo), cpVect(-bo, -bo), segment_height)
        bottom.sensor = true
        objects.add(bottom)
    }

    fun _get_segments_points(): Array<Segment> {
        val points = ArrayList<Segment>()
        getBaseSegments().map { it.heigth = segment_height;it }.forEach {
            points.add(it)
        }
        getAdditionalSegments().map { it.heigth = segment_height;it }.forEach {
            points.add(it)
        }

        getBaseArcs().map {
            val rad_pre_seg = (it.b - it.a) / it.sc
            val parts = ArrayList<Segment>()
            for (i in 0..it.sc) {
                val fpoint_rad = (it.a + rad_pre_seg * i).toDouble()
                val spoint_rad = (it.a + rad_pre_seg * (i + 1)).toDouble()
                val fpoint = cpVect.cpvadd(it.c, cpVect((it.r * Math.cos(fpoint_rad)).toFloat(), (it.r * Math.sin(fpoint_rad)).toFloat()))
                val spoint = cpVect.cpvadd(it.c, cpVect((it.r * Math.cos(spoint_rad)).toFloat(), (it.r * Math.sin(spoint_rad)).toFloat()))
                parts.add(Segment(fpoint, spoint, segment_height))
            }
            parts
        }.forEach { points.addAll(it) }

        return points.toTypedArray()
    }


    fun get_objects_for_space():ArrayList<cpSegmentShape> = this.objects

//    fun get_cars_start_position() = cars_start_position
}

open class PillMap(space: cpSpace) : Map(space) {
    init {
        external_id = 1
    }

    override fun getBaseArcs(): Array<Arc> {
        return arrayOf(Arc(cpVect(300f, 400f), 300f, (Math.PI / 2).toFloat(), (Math.PI * 3 / 2).toFloat(), 30),
                Arc(cpVect(900f, 400f), 300f, (Math.PI / 2).toFloat(), (-Math.PI / 2).toFloat(), 30))
    }

    override fun getBaseSegments(): Array<Segment> {
        return arrayOf(Segment(cpVect(300f, 100f), cpVect(900f, 100f)),
                Segment(cpVect(300f, 700f), cpVect(900f, 700f)))
    }

    override fun getAdditionalArcs(): Array<Arc> = emptyArray()


    override fun getAdditionalSegments(): Array<Segment> = emptyArray()

}

class PillHubbleMap(space: cpSpace) : PillMap(space) {
    init {
        external_id = 2
    }

    override fun getAdditionalArcs(): Array<Arc> = arrayOf(Arc(cpVect(600f, -150f), 300f, (Math.PI / 3.2).toFloat(), (Math.PI / 1.45).toFloat(), 30))
}

class PillHillMap(space: cpSpace) : PillMap(space) {
    init {
        external_id = 3
    }

    override fun getAdditionalArcs(): Array<Arc> = arrayOf(Arc(cpVect(300f, 300f), 200f, (-Math.PI / 2).toFloat(), (-Math.PI / 6).toFloat(), 30),
            Arc(cpVect(900f, 300f), 200f, (Math.PI * 3 / 2).toFloat(), (Math.PI * 7 / 6).toFloat(), 30))

    override fun getAdditionalSegments(): Array<Segment> = arrayOf(Segment(cpVect(465f, 195f), cpVect(735f, 195f)))
}

class PillCarcassMap(space: cpSpace) : PillMap(space) {
    init {
        external_id = 4
    }

    override fun getAdditionalSegments(): Array<Segment> = arrayOf(Segment(cpVect(300f, 400f), cpVect(900f, 400f)))
}

class IslandMap(space: cpSpace) : Map(space) {
    init {
        external_id = 5
    }

    override fun getBaseSegments(): Array<Segment> = arrayOf(Segment(cpVect(100f, 100f), cpVect(1100f, 100f)))

    override fun getAdditionalArcs(): Array<Arc> = emptyArray()
    override fun getAdditionalSegments(): Array<Segment> = emptyArray()
    override fun getBaseArcs(): Array<Arc> = emptyArray()
}

class IslandHoleMap(space: cpSpace) : Map(space) {

    init {
        external_id = 6
    }

    override fun getBaseArcs(): Array<Arc> = arrayOf(Arc(cpVect(300f, 100f), 100f, (Math.PI / 6).toFloat(), (Math.PI / 2).toFloat(), 30),
            Arc(cpVect(900f, 100f), 100f, (Math.PI / 2).toFloat(), (Math.PI * 5 / 6).toFloat(), 30))

    override fun getBaseSegments(): Array<Segment> = arrayOf(Segment(cpVect(10f, 400f), cpVect(50f, 200f)),
            Segment(cpVect(50f, 200f), cpVect(300f, 200f)),

            Segment(cpVect(380f, 150f), cpVect(820f, 150f)),
            Segment(cpVect(900f, 200f), cpVect(1150f, 200f)),
            Segment(cpVect(1150f, 200f), cpVect(1190f, 400f)))

    override fun getAdditionalArcs(): Array<Arc> = emptyArray()
    override fun getAdditionalSegments(): Array<Segment> = emptyArray()
}