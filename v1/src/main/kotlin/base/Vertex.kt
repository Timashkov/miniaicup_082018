package base

data class Vertex(val x: Double,val y: Double){
    override fun toString(): String {
        return "[$x, $y]"
    }

    fun distance(v: Vertex): Double {
        return Math.sqrt(Math.pow(v.x - x, 2.0) + Math.pow(v.y - y, 2.0))
    }
}

fun Vertex.minus(x: Double, y: Double): Vertex = Vertex(this.x-x, this.y - y)
fun Vertex.plus(x: Double, y: Double): Vertex = Vertex(this.x + x, this.y + y)