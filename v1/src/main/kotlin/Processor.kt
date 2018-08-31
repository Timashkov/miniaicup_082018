import org.json.JSONObject
import utils.Logger

class Processor(configJson: JSONObject) {

    private val mLogger = Logger()
    private var mCurrentTick = 0

    init{
        mLogger.writeLog(configJson.toString())
    }

    // Tick Process
    fun onTick(tickData: JSONObject): JSONObject {
        mLogger.writeLog("\nT$mCurrentTick")
        mLogger.writeLog("INCOMING $tickData")
//        val parsed = parseIncoming(tickData)
//        val out = analyzeData(parsed, mCurrentTick)
        val out = JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "Died"))
        mCurrentTick++
        return out
    }

//    fun parseIncoming(tickData: JSONObject): ParseResult =
//            ParseResult(MineInfo(tickData.getJSONArray("Mine"), mWorldConfig, mLogger), WorldObjectsInfo(tickData.getJSONArray("Objects"), mWorldConfig, mLogger), ArrayList())
//
//    fun analyzeData(parseResult: ParseResult, currentTickCount: Int): JSONObject {
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
//    }
//
//    private fun checkTriggers(gameEngine: GameEngine) {
//        mAction = ACTIONS.MINE
//    }
}
