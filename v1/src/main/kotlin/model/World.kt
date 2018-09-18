package model

import org.json.JSONArray
import org.json.JSONObject

class World(private val json: JSONObject) {

    private val mCar = Car(json.getJSONObject("proto_car"))
    private val mEnemyCar = Car()
    private val mMapSegmentsHolder = MapSegmentsHolder(json.getJSONObject("proto_map").getJSONArray("segments"))
    private val mDeadLine = DeadLine()


    override fun toString(): String {
        return "WORLD: $json\nCar: $mCar"
    }

    fun updateCarInfo(myCar: JSONArray?) {

    }

    fun updateDeadlinePosition(dp: Double) {
        mDeadLine.updatePosition(dp)
    }

    fun updateEnemyInfo(enemyInfo: JSONArray?) {

    }

}