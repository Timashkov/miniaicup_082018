package model

import org.json.JSONObject

class World(private val json: JSONObject) {

    private val mCar = Car(json.getJSONObject("proto_car"))
    private val mMapSegmentsHolder = MapSegmentsHolder(json.getJSONObject("proto_map").getJSONArray("segments"))
    override fun toString(): String {
        return "WORLD: $json\nCar: $mCar"
    }


}