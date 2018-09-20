import model.World
import org.json.JSONObject
import utils.Logger

class Processor(private val mWorld: World) {

    private val mLogger = Logger()
    private var mCurrentTick = 0

    init {
        mLogger.writeLog(mWorld.toString())
    }

    // Tick Process
    fun onTick(tickData: JSONObject): JSONObject {
        mLogger.writeLog("\nT$mCurrentTick")
        mLogger.writeLog("INCOMING $tickData")
        parseIncoming(tickData)
        val out = analyzeData()
        // commands = ['left', 'right', 'stop']  # доступные команды
        // rint(json.dumps({"command": cmd, 'debug': cmd})  # отправка результата
        mCurrentTick++
        return out
    }

    fun parseIncoming(tickData: JSONObject){
        val enemyInfo = tickData.getJSONArray("enemy_car")
        val dp = tickData.getDouble("deadline_position")
        val myCar = tickData.getJSONArray("my_car")

        mWorld.updateCarInfo(myCar)
        mWorld.updateDeadlinePosition(dp)
        mWorld.updateEnemyInfo(enemyInfo)
    }

    fun analyzeData(): JSONObject {

//        if (parseResult.mineInfo.isNotEmpty()) {
//            val data = mEvasionFilter.onFilter(parseResult, currentTickCount)
//            try {
//
//                val gameEngine = GameEngine(mWorldConfig, data, currentTickCount, mLogger)
//                mLogger.writeLog("GE Parsed. Start check strategies")
//
//                mDefaultStrategy.addPhantomFood(parseResult.phantomFood, gameEngine.worldParseResult.mineInfo)
//                var strategyResult = mEatEnemyStrategy.apply(gameEngine, mCachedParseResult)
//                mLogger.writeLog("$strategyResult")
//                if (strategyResult.achievementScore > 0) {
//                    mDefaultStrategy.stopStrategy()
//                    mLogger.writeLog("APPLY eat enemy: $strategyResult\n")
//                    return strategyResult.toJSONCommand()
//                }
//
//                strategyResult = mFindFoodV2.apply(gameEngine, mCachedParseResult)
//                if (strategyResult.achievementScore > -1){
//                    mDefaultStrategy.stopStrategy()
//                    mLogger.writeLog("APPLY FF2: $strategyResult\n")
//                    return strategyResult.toJSONCommand()
//                }
//
//                strategyResult = mDefaultStrategy.apply(gameEngine)
//                mLogger.writeLog("$strategyResult")
//                if (strategyResult.achievementScore >= 0) {
//                    mLogger.writeLog("Chosen Default strategy: $strategyResult\n")
//                    return strategyResult.toJSONCommand()
//                }
//
//            } catch (e: Exception) {
//                mLogger.writeLog("Going wrong")
//                mLogger.writeLog("${e.message}")
//            } finally {
//                mCachedParseResult = data
//            }
//        }
//        mLogger.writeLog("DEFAULT DIED")
//        return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "Died"))
        val car = mWorld.getCar()

        val out = JSONObject(mapOf("command" to if(car.isInAir(mWorld.mapSegmentsHolder)) car.stop() else car.turnLeft(), "Debug" to "left"))
        return out
    }
//
//    private fun checkTriggers(gameEngine: GameEngine) {
//        mAction = ACTIONS.MINE
//    }
}


/** `type` — `new_match`
 * `params` — параметры игрового мира
 * `my_lives` - число жизней стратегии
 * `enemy_lives` — число жизней противника
 * `proto_map` — описывает свойства карты
 * `external_id` — внешний id карты
 * `segments` — сегменты карты
 * `proto_car`
 * `external_id` - внешний id машины
 * `button_poly` - полигон кнопки
 * `car_body_poly` - полигон машины
 * `car_body_mass`- масса машины

 * `car_body_friction` - трение кузова машины
 * `car_body_elasticity` - эластичность кузова машины
 * `max_speed` - максимальная угловая скорость колес
 * `max_angular_speed` - максимальная угловая скорость в воздухе (данный параметр больше не используется и скоро будет удален)
 * `torque` - крутящий момент кузова машины в воздухе
 * `drive` - привод машины (передний, задний, полный)

 * `rear_wheel_radius` - радиус заднего колеса
 * `rear_wheel_mass` - масса заднего колеса
 * `rear_wheel_position` - положение заднего колеса в координатах относительно кузова машины
 * `rear_wheel_friction` - трение заднего колеса
 * `rear_wheel_elasticity` - эластичность заднего колеса
 * `rear_wheel_joint` - положение жесткого соединения заднего колеса
 * `rear_wheel_damp_position` - положение пружины заднего колеса
 * `rear_wheel_damp_length` - длина пружины заднего колеса
 * `rear_wheel_damp_stiffness` - жесткость пружины заднего колеса
 * `rear_wheel_damp_damping` - затухание пружины заднего колеса

 * `front_wheel_radius` - радиус переднего колеса
 * `front_wheel_mass` - масса переднего колеса
 * `front_wheel_position` - положение переднего колеса в координатах относительно кузова машины
 * `front_wheel_friction` -  трение переднего колеса
 * `front_wheel_elasticity` - эластичность переднего колеса
 * `front_wheel_joint` - положение жесткого соединения переднего колеса
 * `front_wheel_damp_position` - положение пружины переднего колеса
 * `front_wheel_damp_length` - длина пружины переднего колеса
 * `front_wheel_damp_stiffness` - жесткость пружины переднего колеса
 * `front_wheel_damp_damping` - затухание пружины переднего колеса

Для машины с квадратными колесами еще приходит
 * `squared_wheels` - признак квадратных колес*/


/*{"params":
{"proto_map":
{"external_id":1,
"segments":
[
[[300,100],[900,100],10],
[[300,700],[900,700],10],
[[300,700],[268.641461019704,698.3565686104821],10],
[[268.641461019704,698.3565686104821],[237.6264927546722,693.4442802201418],10],
[[237.6264927546722,693.4442802201418],[207.2949016875158,685.3169548885461],10],
[[207.2949016875158,685.3169548885461],[177.97900707726,674.0636372927803],10],
[[177.97900707726,674.0636372927803],[150.00000000000006,659.8076211353316],10],
[[150.00000000000006,659.8076211353316],[123.66442431225809,642.7050983124842],10],
[[123.66442431225809,642.7050983124842],[99.26081809234262,622.9434476432184],10],
[[99.26081809234262,622.9434476432184],[77.0565523567818,600.7391819076574],10],
[[77.0565523567818,600.7391819076574],[57.29490168751579,576.335575687742],10],
[[57.29490168751579,576.335575687742],[40.192378864668456,550.0000000000001],10],
[[40.192378864668456,550.0000000000001],[25.936362707219757,522.0209929227401],10],
[[25.936362707219757,522.0209929227401],[14.683045111453964,492.70509831248427],10],
[[14.683045111453964,492.70509831248427],[6.55571977985835,462.3735072453279],10],
[[6.55571977985835,462.3735072453279],[1.643431389518014,431.35853898029615],10],
[[1.643431389518014,431.35853898029615],[0,400.00000000000006],10],
[[0,400.00000000000006],[1.643431389517957,368.6414610197041],10],
[[1.643431389517957,368.6414610197041],[6.555719779858293,337.62649275467226],10],
[[6.555719779858293,337.62649275467226],[14.683045111453907,307.2949016875158],10],
[[14.683045111453907,307.2949016875158],[25.9363627072197,277.97900707726006],10],
[[25.9363627072197,277.97900707726006],[40.19237886466834,250.00000000000009],10],
[[40.19237886466834,250.00000000000009],[57.29490168751573,223.6644243122581],10],
[[57.29490168751573,223.6644243122581],[77.05655235678162,199.26081809234262],10],
[[77.05655235678162,199.26081809234262],[99.26081809234248,177.0565523567818],10],
[[99.26081809234248,177.0565523567818],[123.66442431225803,157.2949016875158],10],
[[123.66442431225803,157.2949016875158],[149.99999999999986,140.1923788646685],10],
[[149.99999999999986,140.1923788646685],[177.97900707725972,125.93636270721981],10],
[[177.97900707725972,125.93636270721981],[207.29490168751573,114.68304511145396],10],
[[207.29490168751573,114.68304511145396],[237.62649275467206,106.55571977985835],10],
[[237.62649275467206,106.55571977985835],[268.64146101970374,101.64343138951801],10],
[[268.64146101970374,101.64343138951801],[299.99999999999994,100],10],
[[900,700],[931.3585389802961,698.356568610482],10],
[[931.3585389802961,698.356568610482],[962.3735072453278,693.4442802201416],10],
[[962.3735072453278,693.4442802201416],[992.7050983124842,685.316954888546],10],
[[992.7050983124842,685.316954888546],[1022.0209929227401,674.0636372927802],10],
[[1022.0209929227401,674.0636372927802],[1050,659.8076211353316],10],
[[1050,659.8076211353316],[1076.335575687742,642.7050983124842],10],
[[1076.335575687742,642.7050983124842],[1100.7391819076574,622.9434476432183],10],
[[1100.7391819076574,622.9434476432183],[1122.9434476432182,600.7391819076574],10],
[[1122.9434476432182,600.7391819076574],[1142.7050983124843,576.335575687742],10],
[[1142.7050983124843,576.335575687742],[1159.8076211353316,550],10],
[[1159.8076211353316,550],[1174.0636372927802,522.0209929227401],10],
[[1174.0636372927802,522.0209929227401],[1185.316954888546,492.7050983124842],10],
[[1185.316954888546,492.7050983124842],[1193.4442802201415,462.3735072453278],10],
[[1193.4442802201415,462.3735072453278],[1198.356568610482,431.3585389802961],10],
[[1198.356568610482,431.3585389802961],[1200,400],10],
[[1200,400],[1198.356568610482,368.64146101970397],10],
[[1198.356568610482,368.64146101970397],[1193.4442802201418,337.62649275467226],10],
[[1193.4442802201418,337.62649275467226],[1185.316954888546,307.2949016875158],10],
[[1185.316954888546,307.2949016875158],[1174.0636372927802,277.97900707725995],10],
[[1174.0636372927802,277.97900707725995],[1159.8076211353316,250.00000000000006],10],
[[1159.8076211353316,250.00000000000006],[1142.7050983124843,223.66442431225806],10],
[[1142.7050983124843,223.66442431225806],[1122.9434476432184,199.2608180923426],10],
[[1122.9434476432184,199.2608180923426],[1100.7391819076574,177.05655235678176],10],
[[1100.7391819076574,177.05655235678176],[1076.335575687742,157.29490168751576],10],
[[1076.335575687742,157.29490168751576],[1050,140.19237886466846],10],
[[1050,140.19237886466846],[1022.0209929227401,125.93636270721976],10],
[[1022.0209929227401,125.93636270721976],[992.7050983124842,114.68304511145396],10],
[[992.7050983124842,114.68304511145396],[962.3735072453279,106.55571977985835],10],
[[962.3735072453279,106.55571977985835],[931.3585389802961,101.64343138951801],10],
[[931.3585389802961,101.64343138951801],[900,100],10]]
},

"my_lives":9,
"proto_car":
    {
    "front_wheel_friction":1,
    "front_wheel_damp_damping":900,
    "torque":14000000,
    "front_wheel_damp_position":[122,20],
    "external_id":1,
    "rear_wheel_position":[29,-5],
    "rear_wheel_damp_position":[29,20],
    "front_wheel_mass":5,
    "front_wheel_radius":12,
    "car_body_elasticity":0.5,
    "front_wheel_position":[122,-5],
    "rear_wheel_radius":12,
    "rear_wheel_friction":1,
    "rear_wheel_joint":[150,0],
    "car_body_poly":
        [ [0,6],[0,25],[33,42],[85,42],[150,20],[150,0],[20,0]],
    "rear_wheel_damp_length":25,
    "front_wheel_damp_stiffness":60000,
    "rear_wheel_elasticity":0.8,
    "car_body_friction":0.9,
    "max_angular_speed":2,
    "front_wheel_damp_length":25,
    "button_poly":
        [[40,42],[40,43],[78,43],[78,42]],
    "max_speed":70,
    "front_wheel_elasticity":0.8,
    "front_wheel_joint":[0,6],
    "rear_wheel_damp_damping":3000,
    "rear_wheel_mass":50,
    "car_body_mass":200,
    "rear_wheel_damp_stiffness":50000,
    "drive":2
    },
"enemy_lives":9},
"type":"new_match"}*/

//T0
//INCOMING {"params":{"enemy_car":[[900,300],0,-1,[871,295,0],[778,295,0]],"my_car":[[300,300],0,1,[329,295,0],[422,295,0]],"deadline_position":10},"type":"tick"}
