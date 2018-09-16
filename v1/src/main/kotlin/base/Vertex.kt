package base

data class Vertex(val x: Double,val y: Double){
    override fun toString(): String {
        return "[$x, $y]"
    }
}

fun Vertex.minus(x: Double, y: Double): Vertex = Vertex(this.x-x, this.y - y)
fun Vertex.plus(x: Double, y: Double): Vertex = Vertex(this.x + x, this.y + y)