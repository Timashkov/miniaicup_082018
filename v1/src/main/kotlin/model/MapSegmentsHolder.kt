package model

import base.Vertex
import org.json.JSONArray

class MapSegmentsHolder(jsonArray: JSONArray) {
    val mSegments: ArrayList<MapSegment> = ArrayList()

    init {
        jsonArray.forEach { it ->
            val segmentArray = it as JSONArray

            val v1 = segmentArray.getJSONArray(0)
            val v2 = segmentArray.getJSONArray(1)
            val v3 = segmentArray.getInt(2)

            val vert1 = Vertex(v1.getDouble(0), v1.getDouble(1))
            val vert2 = Vertex(v2.getDouble(0), v2.getDouble(1))
            mSegments.add(MapSegment(vert1, vert2, v3))
        }
    }
}
