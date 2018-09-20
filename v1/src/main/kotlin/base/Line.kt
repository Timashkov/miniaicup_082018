package base

class Line(val v1: Vertex, val v2: Vertex) {
    val k = if (v1.x != v2.x) (v1.y - v2.y) / (v1.x - v2.x) else Double.NaN
    val b = v1.y - k * v1.x

    fun getNormalVertex(p: Vertex): Vertex {

        if (k != 0.0 && k != Double.NaN) {
            val kdiff = -1 / k

            val bdiff = p.y - kdiff * p.x
            val x1 = (b - bdiff) / (kdiff - k)
            val y1 = k * x1 + b
            return Vertex(x1, y1)
        } else if (k == 0.0){
            val y1 = v1.y
            val x1 = p.x
            return Vertex(x1, y1)
        } else {
            val x1 = v1.x
            val y1 = p.y
            return Vertex(x1, y1)
        }
    }

    fun normalDistance(p: Vertex): Double {
        val v = getNormalVertex(p)
        return p.distance(v)
    }
}