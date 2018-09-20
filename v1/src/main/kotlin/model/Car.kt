package model

import base.Line
import base.Vertex
import org.json.JSONArray
import org.json.JSONObject
import utils.Logger
import java.util.*

class Car() {
    private var mTorque: Int = 0
    private var mExternalId: Int = 0
    private var mCarBodyElasticity: Double = 0.0
    private var mCarBodyPoly: Array<Vertex> = emptyArray()
    private var mCarBodyFriction: Double = 0.0
    private var mAngularSpeed: Double = 0.0
    private var mButtonPoly: Array<Vertex> = emptyArray()
    private var mMaxSped: Double = 0.0
    private var mCarBodyMass: Double = 0.0
    private var mDrive: Int = 0
    private var mFrontWheel: Wheel? = null
    private var mRearWheel: Wheel? = null
    private var mSquareWheels: Boolean = false
    private var mLeftBottomPosition: Vertex = Vertex(0.0, 0.0)
    private var mSide = 0
    private var mAngle: Double = 0.0

    constructor(jsonObject: JSONObject) : this() {
        mTorque = jsonObject.getInt("torque")
        mExternalId = jsonObject.getInt("external_id")  // 1 - buggy, 2 - bus, 3 - square buggy
        mCarBodyElasticity = jsonObject.getDouble("car_body_elasticity")
        mCarBodyFriction = jsonObject.getDouble("car_body_friction")
        mAngularSpeed = jsonObject.getDouble("max_angular_speed")
        mMaxSped = jsonObject.getDouble("max_speed")
        mCarBodyMass = jsonObject.getDouble("car_body_mass")
        mDrive = jsonObject.getInt("drive")
        mFrontWheel = Wheel(Wheel.WheelType.FRONT, jsonObject)
        mRearWheel = Wheel(Wheel.WheelType.REAR, jsonObject)
        mSquareWheels = if (jsonObject.has("squared_wheels")) jsonObject.getBoolean("squared_wheels") else false

        val carArray = jsonObject.getJSONArray("car_body_poly")
        mCarBodyPoly = Array(carArray.length()) { i -> Vertex(carArray.getJSONArray(i).getDouble(0), carArray.getJSONArray(i).getDouble(1)) }
        val buttonArray = jsonObject.getJSONArray("button_poly")
        mButtonPoly = Array(buttonArray.length()) { i -> Vertex(buttonArray.getJSONArray(i).getDouble(0), buttonArray.getJSONArray(i).getDouble(1)) }

    }


    override fun toString(): String {
        return "mTorque: $mTorque\nmExternalId: $mExternalId\nmElasticity: $mCarBodyElasticity\nmCarBodyFriction: $mCarBodyFriction" +
                "\nmAngularSpeed: $mAngularSpeed\nmMaxSped: $mMaxSped\nmCarBodyMass: $mCarBodyMass\nmDrive: $mDrive\n" +
                "mButtonPoly: ${Arrays.toString(mButtonPoly)}\nmCarBodyPoly: ${Arrays.toString(mCarBodyPoly)}\n" +
                "mFrontWheel: $mFrontWheel\nmRearWheel: $mRearWheel\nSquaredWheels: $mSquareWheels"
    }

    fun setPosition(x: Double, y: Double) {
        mLeftBottomPosition = Vertex(x, y)
    }

    fun setSide(side: Int) {
        mSide = side
    }

    fun turnLeft(): String {
        return if (mSide == -1) {
            "left"
        } else {
            "right"
        }
    }

    fun turnRight(): String {
        return if (mSide == -1) {
            "right"
        } else {
            "left"
        }
    }

    fun setAngle(angle: Double) {
        mAngle = angle
    }

    fun angle() = mAngle

    fun isInAir(segmentsHolder: MapSegmentsHolder): Boolean {
        mFrontWheel?.let {
            if (!checkWheelInAir(it, segmentsHolder))
                return false
        }

        mRearWheel?.let {
            if (!checkWheelInAir(it, segmentsHolder))
                return false
        }

//        mCarBodyPoly.forEach { polyVertex ->
//            segmentsHolder.mSegments.forEach {
//                val line = Line(it.dot1, it.dot2)
//                val distance = line.normalDistance(polyVertex)
//                if (distance < 10.021){
//                    Logger().writeLog("body no in air Distance $distance}")
//                    return false
//                }
//            }
//        }
        return true
    }

    private fun checkWheelInAir(wheel: Wheel, segmentsHolder: MapSegmentsHolder): Boolean {
        segmentsHolder.mSegments.forEach {
            val line = Line(it.dot1, it.dot2)
            val distance = line.normalDistance(wheel.position())

            if ( distance < (wheel.radius() + 10.021)){
                Logger().writeLog("wheel not in air Distance $distance radius ${wheel.radius()}")
                return false
            }

        }
        return true
    }

    fun setRearWheelInfo(jsonArray: JSONArray) {
        mRearWheel?.updatePosition(jsonArray.getDouble(0), jsonArray.getDouble(1), jsonArray.getDouble(2))
    }

    fun setFrontWheelInfo(jsonArray: JSONArray) {
        mFrontWheel?.updatePosition(jsonArray.getDouble(0), jsonArray.getDouble(1), jsonArray.getDouble(2))
    }

    fun stop() = "stop"


}
//http://www.pymunk.org/en/latest/pymunk.html#pymunk.Space.damping
/*
    "torque":14000000,
    "external_id":1,
    "car_body_elasticity":0.5,
    "car_body_poly":
        [ [0,6],[0,25],[33,42],[85,42],[150,20],[150,0],[20,0]],
    "car_body_friction":0.9,
    "max_angular_speed":2,
    "button_poly":
        [[40,42],[40,43],[78,43],[78,42]],
    "max_speed":70,
    "car_body_mass":200,
    "drive":2

    "front_wheel_friction":1,
    "front_wheel_damp_damping":900,
    "front_wheel_damp_position":[122,20],
    "front_wheel_mass":5,
    "front_wheel_radius":12,
    "front_wheel_position":[122,-5],
    "front_wheel_damp_stiffness":60000,
    "front_wheel_damp_length":25,
    "front_wheel_elasticity":0.8,
    "front_wheel_joint":[0,6],

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