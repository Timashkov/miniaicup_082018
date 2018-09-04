package model

import org.json.JSONObject

class World(private val json: JSONObject) {

    private val mCar = Car(json.getJSONObject("proto_car"))
    override fun toString(): String {
        return "WORLD: $json\nCar: $mCar"
    }
}