package model

import org.json.JSONObject

class World(json: JSONObject) {

    private val mCar = Car(json.getJSONObject("proto_car"))
    override fun toString(): String {
        return "Car: $mCar"
    }
}