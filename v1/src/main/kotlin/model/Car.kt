package model

import base.Vertex
import org.json.JSONObject
import java.util.*

class Car(jsonObject: JSONObject) {
    private var mTorque: Int = jsonObject.getInt("torque")
    private var mExternalId: Int = jsonObject.getInt("external_id")
    private var mCarBodyElasticity: Float = jsonObject.getFloat("car_body_elasticity")
    private var mCarBodyPoly: Array<Vertex>
    private var mCarBodyFriction: Float = jsonObject.getFloat("car_body_friction")
    private var mAngularSpeed: Float = jsonObject.getFloat("max_angular_speed")
    private var mButtonPoly: Array<Vertex>
    private var mMaxSped: Float = jsonObject.getFloat("max_speed")
    private var mCarBodyMass: Float = jsonObject.getFloat("car_body_mass")
    private val mDrive: Int = jsonObject.getInt("drive")
    private val mFrontWheel: Wheel = Wheel(Wheel.WheelType.FRONT, jsonObject)
    private val mRearWheel: Wheel = Wheel(Wheel.WheelType.REAR, jsonObject)
    private val mSquareWheels: Boolean = if (jsonObject.has("squared_wheels")) jsonObject.getBoolean("squared_wheels") else false

    init {
        val carArray = jsonObject.getJSONArray("car_body_poly")
        mCarBodyPoly = Array(carArray.length()) { i -> Vertex(carArray.getJSONArray(i).getFloat(0), carArray.getJSONArray(i).getFloat(1)) }
        val buttonArray = jsonObject.getJSONArray("button_poly")
        mButtonPoly = Array(buttonArray.length()) { i -> Vertex(buttonArray.getJSONArray(i).getFloat(0), buttonArray.getJSONArray(i).getFloat(1)) }
    }

    override fun toString(): String {
        return "mTorque: $mTorque\nmExternalId: $mExternalId\nmElasticity: $mCarBodyElasticity\nmCarBodyFriction: $mCarBodyFriction" +
                "\nmAngularSpeed: $mAngularSpeed\nmMaxSped: $mMaxSped\nmCarBodyMass: $mCarBodyMass\nmDrive: $mDrive\n" +
                "mButtonPoly: ${Arrays.toString(mButtonPoly)}\nmCarBodyPoly: ${Arrays.toString(mCarBodyPoly)}\n" +
                "mFrontWheel: $mFrontWheel\nmRearWheel: $mRearWheel\nSquaredWheels: $mSquareWheels"
    }
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