package base

data class Vertex(val x: Float,val y: Float){
    override fun toString(): String {
        return "[$x, $y]"
    }
}

fun Vertex.minus(x: Float, y: Float): Vertex = Vertex(this.x-x, this.y - y)
fun Vertex.plus(x: Float, y: Float): Vertex = Vertex(this.x + x, this.y + y)