package model

import base.Vertex
import org.json.JSONObject

class Wheel(private val wheelType: WheelType, val jsonObject: JSONObject) {
    private var mRadius: Float = jsonObject.getFloat("${wheelType.prefix}radius")
    private var mMass: Float = jsonObject.getFloat("${wheelType.prefix}mass")
    private var mPosition: Vertex
    private var mElasticity: Float = jsonObject.getFloat("${wheelType.prefix}elasticity")
    private var mFriction: Float = jsonObject.getFloat("${wheelType.prefix}friction")
    private var mJoint: Vertex
    private var mDampDamping: Float = jsonObject.getFloat("${wheelType.prefix}damp_damping")
    private var mDampPosition: Vertex
    private var mDampStiffness: Float = jsonObject.getFloat("${wheelType.prefix}damp_stiffness")
    private var mDampLength = jsonObject.getFloat("${wheelType.prefix}damp_length")
    private var mAngle: Double = 0.0

    init {
        var vertArr = jsonObject.getJSONArray("${wheelType.prefix}position")
        mPosition = Vertex(vertArr.getDouble(0), vertArr.getDouble(1))
        vertArr = jsonObject.getJSONArray("${wheelType.prefix}joint")
        mJoint = Vertex(vertArr.getDouble(0), vertArr.getDouble(1))
        vertArr = jsonObject.getJSONArray("${wheelType.prefix}damp_position")
        mDampPosition = Vertex(vertArr.getDouble(0), vertArr.getDouble(1))
    }

    override fun toString(): String {
        return "Wheel: ${wheelType.id}:\nmMass: $mMass\n" +
                "mRadius: $mRadius\nmPosition: $mPosition\n" +
                "mElasticity: $mElasticity\nmFrictioon: $mFriction\n" +
                "mJoint: $mJoint\nmDampDamping: $mDampDamping\n" +
                "mDampPostition: $mDampPosition\nmDampStiffness: $mDampStiffness\n" +
                "mDampLength: $mDampLength"
    }

    fun updatePosition(x: Double, y: Double, angle: Double){
        mPosition = Vertex(x,y)
        mAngle = angle
    }

    fun position(): Vertex = mPosition
    fun radius() = mRadius

    enum class WheelType(val id: Int, val prefix: String) {
        FRONT(0, "front_wheel_"),
        REAR(1, "rear_wheel_")
    }
}


/*
    "front_wheel_friction":1,
    "front_wheel_mass":5,
    "front_wheel_radius":12,
    "front_wheel_position":[122,-5],
    "front_wheel_elasticity":0.8,
    "front_wheel_joint":[0,6],
    "front_wheel_damp_damping":900,
    "front_wheel_damp_position":[122,20],
    "front_wheel_damp_stiffness":60000,
    "front_wheel_damp_length":25,

*/
/*
    "rear_wheel_position":[29,-5],
    "rear_wheel_damp_position":[29,20],
    "rear_wheel_radius":12,
    "rear_wheel_friction":1,
    "rear_wheel_joint":[150,0],
    "rear_wheel_damp_length":25,
    "rear_wheel_elasticity":0.8,
    "rear_wheel_damp_damping":3000,
    "rear_wheel_mass":50,
    "rear_wheel_damp_stiffness":50000,
    */