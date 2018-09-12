package gameobjects

import chipmunk_bind.*


class DeadLine(val type: Int, max_length: Float, max_height: Float, space: cpSpace) {
    companion object {
        val ASC = 0
        val DESC = 1
    }

    val line = cpSegmentShape()

    init {
        val fp = cpVect(0f, 0f)
        val sp = cpVect(max_length, 0f)
        val line_body = cpBody()
        line_body.cpBodyInit(0f, 0f)
        line_body.cpBodySetType(cpBodyType.CP_BODY_TYPE_KINEMATIC)

        line.init(line_body, fp, sp, 2f)
        line.sensor = true
        line.body?.cpBodySetPosition(if (type == ASC) cpVect(0f, 10f) else cpVect(0f, max_height - 10))
    }

    fun move() {
        val position = line.body?.cpBodyGetPosition() ?: cpVect.cpvzero
        if (type == ASC) {
            line.body?.cpBodySetPosition(cpVect(position.x, position.y + 0.5f))
        } else {
            line.body?.cpBodySetPosition(cpVect(position.x, position.y - 0.5f))
        }
    }

    fun get_object_for_space(): cpSegmentShape {
        return line
    }

    fun get_position(): Float {
        return line.body?.cpBodyGetPosition()?.y?:0f
    }
}



