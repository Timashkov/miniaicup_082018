package gameobjects

import chipmunk_bind.*

abstract class BaseCar(val external_id: Int = 0) {
    companion object {
        const val RIGHT_DIRECTION = 0
        const val LEFT_DIRECTION = 1

        const val FF = 1
        const val FR = 2
        const val AWD = 3
    }

    var car_body_poly = emptyArray<cpVect>()
    var car_body_mass = 100f
    val car_body_friction = 0.9f
    val car_body_elasticity = 0.5f

    var button_hw = cpVect(3f, 30f)
    var button_position = cpVect(0f, 0f)
    val button_angle = 0

    var max_speed = 300f
    val max_angular_speed = 2
    var torque = 20000000f
    var drive = FR

    var rear_wheel_mass = 60f
    var rear_wheel_radius = 10f
    var rear_wheel_position = cpVect(0f, 0f)
    val rear_wheel_friction = 1f
    val rear_wheel_elasticity = 0.8f
    val rear_wheel_joint = cpVect(0f, 0f)
    val rear_wheel_groove_offset = 5f
    var rear_wheel_damp_position = cpVect(0f, 0f)
    var rear_wheel_damp_length = 20f
    var rear_wheel_damp_stiffness = 6e4f
    var rear_wheel_damp_damping = 1e3f

    var front_wheel_mass = 60f
    var front_wheel_radius = 10f
    var front_wheel_position = cpVect(0f, 0f)
    val front_wheel_friction = 1f
    val front_wheel_elasticity = 0.8f
    val front_wheel_joint = cpVect(0f, 0f)
    val front_wheel_groove_offset = 5f
    var front_wheel_damp_position = cpVect(0f, 0f)
    var front_wheel_damp_length = 20f
    val front_wheel_damp_stiffness = 6e4f
    val front_wheel_damp_damping = 0.9e3f

    var x_modification = 1
    var car_body: cpBody? = null
    var car_shape: cpShape? = null
    var button_shape: cpShape? = null

    var car_group: cpGroup? = null
    var point_query_nearest: IPointQueryListener? = null
    var button_collision_type = cpCollisionType(0)

    private var rear_wheel_body: cpBody? = null
    private var front_wheel_body: cpBody? = null
    private var rear_wheel_motor: cpSimpleMotor? = null
    private var front_wheel_motor: cpSimpleMotor? = null
    private var rear_wheel_objects: ArrayList<cpObject> = ArrayList()
    private var front_wheel_objects: ArrayList<cpObject> = ArrayList()

    private var motors = ArrayList<cpSimpleMotor>()

    fun setup(car_group: cpGroup, direction: Int, point_query_nearest: IPointQueryListener) {
        this.car_group = car_group
        button_collision_type = cpCollisionType(car_group.groupId * 10)
        x_modification = if (direction == RIGHT_DIRECTION) 1 else -1

        car_body = create_car_body()
        car_shape = create_car_shape()
        button_shape = create_button_shape()

        car_body?.cpBodySetCenterOfGravity(car_shape?.cpShapeGetCenterOfGravity() ?: cpVect.cpvzero)

        create_wheel(false)
        create_wheel(true)


        rear_wheel_motor?.let {
            motors.add(it)
        }
        front_wheel_motor?.let {
            motors.add(it)
        }

        this.point_query_nearest = point_query_nearest
    }


    fun create_wheel(isFront: Boolean) {

        val wheel_objects = ArrayList<cpObject>()

        val wheel_mass = if (isFront) front_wheel_mass else rear_wheel_mass
        val wheel_radius = if (isFront) front_wheel_radius else rear_wheel_radius
        val wheel_position = if (isFront) front_wheel_position else rear_wheel_position

        val wheel_friction = if (isFront) front_wheel_friction else rear_wheel_friction
        val wheel_elasticity = if (isFront) front_wheel_elasticity else rear_wheel_elasticity
        val wheel_groove_offset = if (isFront) front_wheel_groove_offset else rear_wheel_groove_offset
        val wheel_damp_position = if (isFront) front_wheel_damp_position else rear_wheel_damp_position
        val wheel_damp_length = if (isFront) front_wheel_damp_length else rear_wheel_damp_length
        val wheel_damp_stiffness = if (isFront) front_wheel_damp_stiffness else rear_wheel_damp_stiffness
        val wheel_damp_damping = if (isFront) front_wheel_damp_damping else rear_wheel_damp_damping

        val wheel_body = cpBody()
        wheel_body.cpBodyInit(wheel_mass, cpMomentForCircle(wheel_mass, 0f, wheel_radius))
        wheel_body.cpBodySetPosition(cpVect(wheel_position.x * x_modification, wheel_position.y))

        val wheel_shape = cpCircleShape(wheel_body, wheel_radius)
        car_group?.let {
            wheel_shape.filter.group = it
        }
//        wheel_shape.color = 255, 34, 150
        wheel_shape.cpShapeSetFriction(wheel_friction)
        wheel_shape.cpShapeSetElasticity(wheel_elasticity)
        wheel_objects.add(wheel_shape)

        val wheel_groove = cpGrooveJoint(car_body!!, wheel_body,
                cpVect(wheel_damp_position.x * x_modification, wheel_damp_position.y - wheel_groove_offset),
                cpVect(wheel_damp_position.x * x_modification, wheel_damp_position.y - wheel_damp_length * 1.5f),
                cpVect(0f, 0f))
        wheel_objects.add(wheel_groove)

        val wheel_damp = cpDampedSpring(wheel_body, car_body!!, cpVect(0f, 0f),
                cpVect(wheel_damp_position.x * x_modification, wheel_damp_position.y),
                wheel_damp_length,
                wheel_damp_stiffness,
                wheel_damp_damping)
        wheel_objects.add(wheel_damp)

        var wheel_motor: cpSimpleMotor? = null
        if ((!isFront && drive in arrayOf(AWD, FR)) || (isFront && drive in arrayOf(AWD, FF))) {
            wheel_motor = cpSimpleMotor(wheel_body, car_body!!, 0f)
        }

        if (isFront) {
            front_wheel_body = wheel_body
            front_wheel_motor = wheel_motor
            front_wheel_objects = wheel_objects
        } else {
            rear_wheel_body = wheel_body
            rear_wheel_motor = wheel_motor
            rear_wheel_objects = wheel_objects
        }
    }

    fun create_car_body(): cpBody {
        val body = cpBody()
        val poly = processed_car_body_poly()
        body.cpBodyInit(car_body_mass, cpMomentForPoly(car_body_mass, poly.size, poly, cpVect.cpvzero, 0f))
        return body
    }

    fun processed_car_body_poly(): Array<cpVect> {
        return car_body_poly.map {
            cpVect(it.x * x_modification, it.y)
        }.toTypedArray()
    }

    fun create_car_shape(): cpShape {
        if (car_body == null)
            throw Exception("Create car body before")

        val car_shape = cpPolyShape()
        car_shape.init(car_body!!, processed_car_body_poly())
        car_shape.cpShapeSetFriction(car_body_friction)
        car_shape.cpShapeSetElasticity(car_body_elasticity)
        car_group?.let {
            car_shape.filter = cpShapeFilter()
            car_shape.filter.group = it
        }
        return car_shape
    }

    fun create_button_shape(): cpShape {
        if (car_body == null) throw Exception("Create car body before")

        val button_shape = cpPolyShape()
        button_shape.init(car_body!!, get_button_poly().map{ it-> cpVect(x_modification*it.x, it.y)}.toTypedArray())

//        button_shape.color = 23, 230, 230
        car_group?.let {
            button_shape.filter = cpShapeFilter()
            button_shape.filter.group = it
        }
        button_shape.sensor = true
        button_shape.cpShapeSetCollisionType(button_collision_type)

        return button_shape
    }

    fun go_right() {
        if (in_air()) car_body?.cpBodySetTorque(torque)

        motors.forEach {
            it.rate = -max_speed
        }
    }

    fun go_left() {
        if (in_air()) car_body?.cpBodySetTorque(-torque)

        motors.forEach {
            it.rate = max_speed
        }
    }

    fun stop() {
        motors.forEach {
            it.rate = 0f
        }

    }

    fun in_air(): Boolean {
        point_query_nearest?.let { listener ->
//            return !(listener.point_query_nearest(rear_wheel_body.position, rear_wheel_radius + 1, cpShapeFilter(car_group!!))
//                    || listener.point_query_nearest(front_wheel_body.position, front_wheel_radius + 1, cpShapeFilter(car_group!!)))
            if (listener.point_query_nearest(rear_wheel_body!!.cpBodyGetPosition(), rear_wheel_radius + 1, cpShapeFilter(car_group!!)) == null)
                return true
            if (listener.point_query_nearest(front_wheel_body!!.cpBodyGetPosition(), front_wheel_radius + 1, cpShapeFilter(car_group!!)) == null)
                return true
        }
        return false
    }

    fun get_button_collision_type(): cpCollisionType {
        return button_collision_type
    }

    fun get_button_poly(): Array<cpVect> {
        val posxy = button_position
        val sizehw = button_hw //h,w
        val cos = Math.cos(button_angle.toDouble())
        val sin = Math.sin(button_angle.toDouble())
        return arrayOf(
                cpVect(posxy.x, posxy.y),
                cpVect((posxy.x + 0 * cos - sizehw.x * sin).toFloat(), (posxy.y + +0 * sin + sizehw.x * cos).toFloat()),
                cpVect((posxy.x + sizehw.y * cos - sizehw.x * sin).toFloat(), (posxy.y + +sizehw.y * sin + sizehw.x * cos).toFloat()),
                cpVect((posxy.x + sizehw.y * cos - 0 * sin).toFloat(), (posxy.y + +sizehw.y * sin + 0 * cos).toFloat())
        )
    }
//    fun fast_dump() {
//        return [(self.car_body.position.x, self.car_body.position.y),
//        self.car_body.angle, self.x_modification,
//        (self.rear_wheel_body.position.x, self.rear_wheel_body.position.y, self.rear_wheel_body.angle),
//        (self.front_wheel_body.position.x, self.front_wheel_body.position.y, self.front_wheel_body.angle)]
//    }

    fun get_objects_for_space_at(point: cpVect): List<cpObject> {
        car_body?.cpBodySetPosition(point)
        front_wheel_body?.cpBodySetPosition(cpVect.cpvadd(point, cpVect(front_wheel_position.x * x_modification, front_wheel_position.y)))
        rear_wheel_body?.cpBodySetPosition(cpVect.cpvadd(point, cpVect(rear_wheel_position.x * x_modification, rear_wheel_position.y)))
        val ret = ArrayList<cpObject>()
        ret.add(button_shape!!)
        ret.add(car_body!!)
        ret.add(car_shape!!)
        ret.add(rear_wheel_body!!)
        ret.add(front_wheel_body!!)
        ret.addAll(rear_wheel_objects)
        ret.addAll(front_wheel_objects)
        ret.addAll(motors)
        return ret
//        return list(chain([self.button_shape, self.car_body, self.car_shape, self.rear_wheel_body, self.front_wheel_body], self.rear_wheel_objects, self.front_wheel_objects, self.motors))
    }


}

/*
class Car(object):


    def __init__(self, car_group, direction, point_query_nearest):
        self.car_group = car_group
        self.button_collision_type = car_group * 10
        self.x_modification = 1 if direction == self.RIGHT_DIRECTION else -1

        self.car_body = self.create_car_body()
        self.car_shape = self.create_car_shape()
        self.button_shape = self.create_button_shape()

        self.car_body.center_of_gravity = Vec2d(self.car_shape.center_of_gravity)

        self.rear_wheel_body, self.rear_wheel_motor, self.rear_wheel_objects = self.create_wheel('rear')
        self.front_wheel_body, self.front_wheel_motor, self.front_wheel_objects = self.create_wheel('front')

        self.motors = []

        if self.rear_wheel_motor:
            self.motors.append(self.rear_wheel_motor)
        if self.front_wheel_motor:
            self.motors.append(self.front_wheel_motor)

        self.point_query_nearest = point_query_nearest

    def create_wheel(self, wheel_side):
        if wheel_side not in ['rear', 'front']:
            raise Exception('Wheel position must be front or rear')
        wheel_objects = []

        wheel_mass = getattr(self, wheel_side + '_wheel_mass')
        wheel_radius = getattr(self, wheel_side + '_wheel_radius')
        wheel_position = getattr(self, wheel_side + '_wheel_position')
        wheel_friction = getattr(self, wheel_side + '_wheel_friction')
        wheel_elasticity = getattr(self, wheel_side + '_wheel_elasticity')
        wheel_groove_offset = getattr(self, wheel_side + '_wheel_groove_offset')
        wheel_damp_position = getattr(self, wheel_side + '_wheel_damp_position')
        wheel_damp_length = getattr(self, wheel_side + '_wheel_damp_length')
        wheel_damp_stiffness = getattr(self, wheel_side + '_wheel_damp_stiffness')
        wheel_damp_damping = getattr(self, wheel_side + '_wheel_damp_damping')

        wheel_body = pymunk.Body(wheel_mass, pymunk.moment_for_circle(wheel_mass, 0, wheel_radius))
        wheel_body.position = (wheel_position[0] * self.x_modification, wheel_position[1])

        wheel_shape = pymunk.Circle(wheel_body, wheel_radius)
        wheel_shape.filter = pymunk.ShapeFilter(group=self.car_group)
        wheel_shape.color = 255, 34, 150
        wheel_shape.friction = wheel_friction
        wheel_shape.elasticity = wheel_elasticity
        wheel_objects.append(wheel_shape)

        wheel_groove = pymunk.GrooveJoint(self.car_body, wheel_body,
                                         (wheel_damp_position[0] * self.x_modification, wheel_damp_position[1] - wheel_groove_offset),
                                         (wheel_damp_position[0] * self.x_modification,
                                          wheel_damp_position[1] - wheel_damp_length * 1.5),
                                         (0, 0))
        wheel_objects.append(wheel_groove)

        wheel_damp = pymunk.DampedSpring(wheel_body, self.car_body, anchor_a=(0, 0),
                                         anchor_b=(wheel_damp_position[0] * self.x_modification, wheel_damp_position[1]),
                                         rest_length=wheel_damp_length,
                                         stiffness=wheel_damp_stiffness,
                                         damping=wheel_damp_damping)
        wheel_objects.append(wheel_damp)

        wheel_motor = None
        if (wheel_side == 'rear' and self.drive in [self.AWD, self.FR]) or (wheel_side == 'front' and self.drive in [self.AWD, self.FF]):
            wheel_motor = pymunk.SimpleMotor(wheel_body, self.car_body, 0)

        return wheel_body, wheel_motor, wheel_objects

    def processed_car_body_poly(self):
        return [(x[0] * self.x_modification, x[1]) for x in self.car_body_poly]

    @classmethod
    def get_button_poly(cls):
        x, y = cls.button_position
        h, w = cls.button_hw
        cos = math.cos(cls.button_angle)
        sin = math.sin(cls.button_angle)
        return [
            (x, y),
            ((x + 0 * cos - h * sin), y + +0 * sin + h * cos),
            ((x + w * cos - h * sin), y + +w * sin + h * cos),
            ((x + w * cos - 0 * sin), y + +w * sin + 0 * cos),
        ]

    def get_objects_for_space_at(self, point):
        self.car_body.position = point
        self.front_wheel_body.position = point + (self.front_wheel_position[0] * self.x_modification, self.front_wheel_position[1])
        self.rear_wheel_body.position = point + (self.rear_wheel_position[0] * self.x_modification, self.rear_wheel_position[1])

        return list(chain([self.button_shape, self.car_body, self.car_shape, self.rear_wheel_body, self.front_wheel_body], self.rear_wheel_objects, self.front_wheel_objects, self.motors))


    @classmethod
    def proto_dump(cls, visio=False):
        base_car_proto = {
            'car_body_poly': cls.car_body_poly,
            'rear_wheel_radius': cls.rear_wheel_radius,
            'front_wheel_radius': cls.front_wheel_radius,
            'button_poly': cls.get_button_poly(),
            'external_id': cls.external_id,

        }

        if not visio:
            extended_car_proto = {
                'car_body_mass': cls.car_body_mass,
                'car_body_friction': cls.car_body_friction,
                'car_body_elasticity': cls.car_body_elasticity,
                'max_speed': cls.max_speed,
                'max_angular_speed': cls.max_angular_speed,
                'torque': cls.torque,
                'drive': cls.drive,

                'rear_wheel_mass': cls.rear_wheel_mass,
                'rear_wheel_position': cls.rear_wheel_position,
                'rear_wheel_friction': cls.rear_wheel_friction,
                'rear_wheel_elasticity': cls.rear_wheel_elasticity,
                'rear_wheel_joint': cls.rear_wheel_joint,
                'rear_wheel_groove_offset': cls.rear_wheel_groove_offset,
                'rear_wheel_damp_position': cls.rear_wheel_damp_position,
                'rear_wheel_damp_length': cls.rear_wheel_damp_length,
                'rear_wheel_damp_stiffness': cls.rear_wheel_damp_stiffness,
                'rear_wheel_damp_damping': cls.rear_wheel_damp_damping,

                'front_wheel_mass': cls.front_wheel_mass,
                'front_wheel_position': cls.front_wheel_position,
                'front_wheel_friction': cls.front_wheel_friction,
                'front_wheel_elasticity': cls.front_wheel_elasticity,
                'front_wheel_joint': cls.front_wheel_joint,
                'front_wheel_groove_offset': cls.front_wheel_groove_offset,
                'front_wheel_damp_position': cls.front_wheel_damp_position,
                'front_wheel_damp_length': cls.front_wheel_damp_length,
                'front_wheel_damp_stiffness': cls.front_wheel_damp_stiffness,
                'front_wheel_damp_damping': cls.front_wheel_damp_damping,
            }
            base_car_proto.update(extended_car_proto)

        return base_car_proto
*/



class Buggy : BaseCar(1) {
    init {
        car_body_poly = arrayOf(
                cpVect(0f, 6f),
                cpVect(0f, 25f),
                cpVect(33f, 42f),
                cpVect(85f, 42f),
                cpVect(150f, 20f),
                cpVect(150f, 0f),
                cpVect(20f, 0f))

        car_body_mass = 200f

        button_position = cpVect(40f, 42f)
        button_hw = cpVect(1f, 38f)

        max_speed = 70f
        torque = 14000000f

        drive = FR

        rear_wheel_mass = 50f
        rear_wheel_position = cpVect(29f, -5f)
        rear_wheel_damp_position = cpVect(29f, 20f)
        rear_wheel_damp_stiffness = 5e4f
        rear_wheel_damp_damping = 3e3f
        rear_wheel_damp_length = 25f
        rear_wheel_radius = 12f

        front_wheel_mass = 5f
        front_wheel_position = cpVect(122f, -5f)
        front_wheel_damp_position = cpVect(122f, 20f)
        front_wheel_damp_length = 25f
        front_wheel_radius = 12f
    }
}
/*

class Bus(Car):
    external_id = 2

    car_body_poly = [
        (0, 6),
        (8, 62),
        (136, 62),
        (153, 32),
        (153, 5),
        (110, 0),
        (23, 0)
    ]
    car_body_mass = 700

    button_position = (137, 59)
    button_angle = -math.atan(3/1.7)
    button_hw = (1, 28)

    max_speed = 45
    torque = 35000000

    drive = Car.AWD

    rear_wheel_radius = 13
    rear_wheel_position = (38, -5)
    rear_wheel_friction = 0.9
    rear_wheel_damp_position = (38, 30)
    rear_wheel_damp_length = 35
    rear_wheel_damp_stiffness = 10e4
    rear_wheel_damp_damping = 6e3

    front_wheel_radius = 13
    front_wheel_position = (125, -5)
    front_wheel_damp_position = (125, 30)
    front_wheel_damp_length = 35
    front_wheel_damp_stiffness = 10e4
    front_wheel_damp_damping = 6e3


class SquareWheelsBuggy(Buggy):
    external_id = 3

    max_speed = 50

    drive = Buggy.AWD

    car_body_mass = 230
    rear_wheel_mass = 10
    rear_wheel_damp_stiffness = 10e4
    rear_wheel_damp_damping = .9e3

    front_wheel_mass = 10

    def create_wheel(self, wheel_side):
        if wheel_side not in ['rear', 'front']:
            raise Exception('Wheel position must be front or rear')
        wheel_objects = []

        wheel_mass = getattr(self, wheel_side + '_wheel_mass')
        wheel_radius = getattr(self, wheel_side + '_wheel_radius')
        wheel_position = getattr(self, wheel_side + '_wheel_position')
        wheel_friction = getattr(self, wheel_side + '_wheel_friction')
        wheel_elasticity = getattr(self, wheel_side + '_wheel_elasticity')
        wheel_groove_offset = getattr(self, wheel_side + '_wheel_groove_offset')
        wheel_damp_position = getattr(self, wheel_side + '_wheel_damp_position')
        wheel_damp_length = getattr(self, wheel_side + '_wheel_damp_length')
        wheel_damp_stiffness = getattr(self, wheel_side + '_wheel_damp_stiffness')
        wheel_damp_damping = getattr(self, wheel_side + '_wheel_damp_damping')

        wheel_body = pymunk.Body(wheel_mass, pymunk.moment_for_box(wheel_mass, (wheel_radius * 2, wheel_radius * 2)))
        wheel_body.position = (wheel_position[0] * self.x_modification, wheel_position[1])

        wheel_shape = pymunk.Poly.create_box(wheel_body, (wheel_radius * 2, wheel_radius * 2))
        wheel_shape.filter = pymunk.ShapeFilter(group=self.car_group)
        wheel_shape.color = 255, 34, 150
        wheel_shape.friction = wheel_friction
        wheel_shape.elasticity = wheel_elasticity
        wheel_objects.append(wheel_shape)

        wheel_groove = pymunk.GrooveJoint(self.car_body, wheel_body,
                                         (wheel_damp_position[0] * self.x_modification, wheel_damp_position[1] - wheel_groove_offset),
                                         (wheel_damp_position[0] * self.x_modification,
                                          wheel_damp_position[1] - wheel_damp_length * 1.5),
                                         (0, 0))
        wheel_objects.append(wheel_groove)

        wheel_damp = pymunk.DampedSpring(wheel_body, self.car_body, anchor_a=(0, 0),
                                         anchor_b=(wheel_damp_position[0] * self.x_modification, wheel_damp_position[1]),
                                         rest_length=wheel_damp_length,
                                         stiffness=wheel_damp_stiffness,
                                         damping=wheel_damp_damping)
        wheel_objects.append(wheel_damp)

        wheel_motor = None
        if (wheel_side == 'rear' and self.drive in [self.AWD, self.FR]) or (wheel_side == 'front' and self.drive in [self.AWD, self.FF]):
            wheel_motor = pymunk.SimpleMotor(wheel_body, self.car_body, 0)

        return wheel_body, wheel_motor, wheel_objects

    @classmethod
    def proto_dump(cls, visio=False):
        proto = super().proto_dump(visio)
        proto['squared_wheels'] = True
        return proto
*/