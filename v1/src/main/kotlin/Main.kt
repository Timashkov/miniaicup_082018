//import chipmunk_bind.chipmunk_binding
import gameobjects.Game
import model.World
import org.json.JSONObject
import utils.Logger

fun main(args: Array<String>) {
    var mProcessor:Processor? = null
//
//
//    val g = Game()
//    g.begin()
//    g.next_match()
//    for (i in 0..5){
//
//        g.tick()
//    }


    while (true) {
        val lineData = JSONObject(readLine())
        Logger().writeLog("Start")
        when(lineData.getString("type")){
            "new_match"->{
                val world = World(lineData.getJSONObject("params"))
                mProcessor = Processor(world)
                Logger().writeLog("Start-new match")
            }
            "tick"->{
                val move = mProcessor?.onTick(lineData.getJSONObject("params"))
                println(move)
                Logger().writeLog(move.toString())
            }
        }
    }
}
