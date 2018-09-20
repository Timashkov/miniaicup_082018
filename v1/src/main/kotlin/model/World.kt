package model

import org.json.JSONArray
import org.json.JSONObject

class World(private val json: JSONObject) {

    private val mCar = Car(json.getJSONObject("proto_car"))
    private val mEnemyCar = Car()
    val mapSegmentsHolder = MapSegmentsHolder(json.getJSONObject("proto_map").getJSONArray("segments"))
    private val mDeadLine = DeadLine()


    override fun toString(): String {
        return "WORLD: $json\nCar: $mCar"
    }

    fun updateCarInfo(myCarInfo: JSONArray) {
//[[300,300],0,1,[329,295,0],[422,295,0]]
        mCar.setPosition(myCarInfo.getJSONArray(0).getDouble(0), myCarInfo.getJSONArray(0).getDouble(1))
        mCar.setSide(myCarInfo.getInt(2))
        mCar.setAngle(myCarInfo.getDouble(1))
        mCar.setRearWheelInfo(myCarInfo.getJSONArray(3))
        mCar.setFrontWheelInfo(myCarInfo.getJSONArray(4))
    }

    fun updateDeadlinePosition(dp: Double) {
        mDeadLine.updatePosition(dp)
    }

    fun updateEnemyInfo(enemyInfo: JSONArray) {
        mEnemyCar.setPosition(enemyInfo.getJSONArray(0).getDouble(0), enemyInfo.getJSONArray(0).getDouble(1))
        mEnemyCar.setSide(enemyInfo.getInt(2))
        mEnemyCar.setAngle(enemyInfo.getDouble(1))
        mEnemyCar.setRearWheelInfo(enemyInfo.getJSONArray(3))
        mEnemyCar.setFrontWheelInfo(enemyInfo.getJSONArray(4))
    }

    fun getCar(): Car = mCar


}