package model

import base.Vertex

class Wheel {
    private var mRadius: Float = 0f
    private var mMass: Float = 0f
    private var mPosition: Vertex = Vertex(0f, 0f)
    private var mElasticy: Float = 0f
    private var mFriction: Float = 0f
    private var mJoint: Vertex = Vertex(0f, 0f)
    private var mDampDamping: Float = 0f
    private var mDampPosition: Float = 0f
    private var mDampStiffness: Float = 0f
    private var mDampLength = 0f
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