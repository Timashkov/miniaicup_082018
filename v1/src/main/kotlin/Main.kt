import org.json.JSONObject

fun main(args: Array<String>) {
    val config = JSONObject(readLine())
    val processor = Processor(config)

    while (true) {
        val tickData = JSONObject(readLine())
        val move = processor.onTick(tickData)
        println(move)
    }
}
