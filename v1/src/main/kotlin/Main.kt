//import chipmunk_bind.chipmunk_binding
import gameobjects.Game
import model.World
import org.json.JSONObject

fun main(args: Array<String>) {
    var mProcessor:Processor? = null


    val g = Game()
    g.begin()
    g.next_match()
    for (i in 0..5){

        g.tick()
    }


    while (true) {
        val lineData = JSONObject(readLine())
        when(lineData.getString("type")){
            "new_match"->{
                val world = World(lineData.getJSONObject("params"))
                mProcessor = Processor(world)
            }
            "tick"->{
                val move = mProcessor?.onTick(lineData.getJSONObject("params"))
                println(move)
            }
        }
    }
}


/*Алексей Дичковский {Commandos}, [02.09.18 20:19]
  [Forwarded from Boris Kolganov]
  Добавили новый параметр к машине - torque. Теперь в воздухе к машине применяется крутящий момент, а не угловая скорость.

  Алексей Дичковский {Commandos}, [02.09.18 20:19]
  [Forwarded from Boris Kolganov]
  Так же добавили сопротивление среды = 0.85
  Подробнее http://www.pymunk.org/en/latest/pymunk.html#pymunk.Space.damping*/