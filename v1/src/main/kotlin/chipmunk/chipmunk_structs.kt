package chipmunk
//
//class cpTransform {
//    var a: Float = 0f
//    var b: Float = 0f
//    var c: Float = 0f
//    var d: Float = 0f
//    var tx: Float = 0f
//    var ty: Float = 0f
//}
//
//class cpVect(){
//    constructor(x: Float, y: Float):this(){
//        this.x = x
//        this.y = y
//    }
//    var x : Float = 0.0f
//    var y : Float = 0.0f
//}
//
//class cpBody {
//    // Integration functions
//    var velocity_func:cpBodyVelocityFunc?=null
//    var position_func:cpBodyPositionFunc?=null
//
//    // mass and it's inverse
//    var m : Float = 0.0f
//    var m_inv : Float = 0.0f
//
//    // moment of inertia and it's inverse
//    var i : Float = 0.0f
//    var i_inv : Float = 0.0f
//
//    // center of gravity
//    var cog = cpVect()
//
//    // position, velocity, force
//    var p = cpVect()
//    var v = cpVect()
//    var f = cpVect()
//
//    // Angle, angular velocity, torque (radians)
//    var a: Float = 0f
//    var w: Float = 0f
//    var t: Float = 0f
//
//    var transform = cpTransform()
//
//    cpDataPointer userData;
//
//    // "pseudo-velocities" used for eliminating overlap.
//    // Erin Catto has some papers that talk about what these are.
//    var v_bias: Float = 0f
//    var w_bias: Float = 0f
//
//    cpSpace *space;
//
//    cpShape *shapeList;
//    cpArbiter *arbiterList;
//    cpConstraint *constraintList;
//
//    struct {
//        cpBody *root;
//        cpBody *next;
//        cpFloat idleTime;
//    } sleeping;
//};
//
//enum cpArbiterState {
//    // Arbiter is active and its the first collision.
//    CP_ARBITER_STATE_FIRST_COLLISION,
//    // Arbiter is active and its not the first collision.
//    CP_ARBITER_STATE_NORMAL,
//    // Collision has been explicitly ignored.
//    // Either by returning false from a begin collision handler or calling cpArbiterIgnore().
//    CP_ARBITER_STATE_IGNORE,
//    // Collison is no longer active. A space will cache an arbiter for up to cpSpace.collisionPersistence more steps.
//    CP_ARBITER_STATE_CACHED,
//    // Collison arbiter is invalid because one of the shapes was removed.
//    CP_ARBITER_STATE_INVALIDATED,
//};
//
//struct cpArbiterThread {
//    struct cpArbiter *next, *prev;
//};
//
//struct cpContact {
//    cpVect r1, r2;
//
//    cpFloat nMass, tMass;
//    cpFloat bounce; // TODO: look for an alternate bounce solution.
//
//    cpFloat jnAcc, jtAcc, jBias;
//    cpFloat bias;
//
//    cpHashValue hash;
//};
//
//struct cpCollisionInfo {
//    const cpShape *a, *b;
//    cpCollisionID id;
//
//    cpVect n;
//
//    int count;
//    // TODO Should this be a unique struct type?
//    struct cpContact *arr;
//};
//
//struct cpArbiter {
//    cpFloat e;
//    cpFloat u;
//    cpVect surface_vr;
//
//    cpDataPointer data;
//
//    const cpShape *a, *b;
//    cpBody *body_a, *body_b;
//    struct cpArbiterThread thread_a, thread_b;
//
//    int count;
//    struct cpContact *contacts;
//    cpVect n;
//
//    // Regular, wildcard A and wildcard B collision handlers.
//    cpCollisionHandler *handler, *handlerA, *handlerB;
//    cpBool swapped;
//
//    cpTimestamp stamp;
//    enum cpArbiterState state;
//};
//
//struct cpShapeMassInfo {
//    cpFloat m;
//    cpFloat i;
//    cpVect cog;
//    cpFloat area;
//};
//
//typedef enum cpShapeType{
//    CP_CIRCLE_SHAPE,
//    CP_SEGMENT_SHAPE,
//    CP_POLY_SHAPE,
//    CP_NUM_SHAPES
//} cpShapeType;
//
//typedef cpBB (*cpShapeCacheDataImpl)(cpShape *shape, cpTransform transform);
//typedef void (*cpShapeDestroyImpl)(cpShape *shape);
//typedef void (*cpShapePointQueryImpl)(const cpShape *shape, cpVect p, cpPointQueryInfo *info);
//typedef void (*cpShapeSegmentQueryImpl)(const cpShape *shape, cpVect a, cpVect b, cpFloat radius, cpSegmentQueryInfo *info);
//
//typedef struct cpShapeClass cpShapeClass;
//
//struct cpShapeClass {
//    cpShapeType type;
//
//    cpShapeCacheDataImpl cacheData;
//    cpShapeDestroyImpl destroy;
//    cpShapePointQueryImpl pointQuery;
//    cpShapeSegmentQueryImpl segmentQuery;
//};
//
//struct cpShape {
//    const cpShapeClass *klass;
//
//    cpSpace *space;
//    cpBody *body;
//    struct cpShapeMassInfo massInfo;
//    cpBB bb;
//
//    cpBool sensor;
//
//    cpFloat e;
//    cpFloat u;
//    cpVect surfaceV;
//
//    cpDataPointer userData;
//
//    cpCollisionType type;
//    cpShapeFilter filter;
//
//    cpShape *next;
//    cpShape *prev;
//
//    cpHashValue hashid;
//};
//
//struct cpCircleShape {
//    cpShape shape;
//
//    cpVect c, tc;
//    cpFloat r;
//};
//
//struct cpSegmentShape {
//    cpShape shape;
//
//    cpVect a, b, n;
//    cpVect ta, tb, tn;
//    cpFloat r;
//
//    cpVect a_tangent, b_tangent;
//};
//
//struct cpSplittingPlane {
//    cpVect v0, n;
//};
//
//#define CP_POLY_SHAPE_INLINE_ALLOC 6
//
//struct cpPolyShape {
//    cpShape shape;
//
//    cpFloat r;
//
//    int count;
//    // The untransformed planes are appended at the end of the transformed planes.
//    struct cpSplittingPlane *planes;
//
//    // Allocate a small number of splitting planes internally for simple poly.
//    struct cpSplittingPlane _planes[2*CP_POLY_SHAPE_INLINE_ALLOC];
//};
//
//typedef void (*cpConstraintPreStepImpl)(cpConstraint *constraint, cpFloat dt);
//typedef void (*cpConstraintApplyCachedImpulseImpl)(cpConstraint *constraint, cpFloat dt_coef);
//typedef void (*cpConstraintApplyImpulseImpl)(cpConstraint *constraint, cpFloat dt);
//typedef cpFloat (*cpConstraintGetImpulseImpl)(cpConstraint *constraint);
//
//typedef struct cpConstraintClass {
//    cpConstraintPreStepImpl preStep;
//    cpConstraintApplyCachedImpulseImpl applyCachedImpulse;
//    cpConstraintApplyImpulseImpl applyImpulse;
//    cpConstraintGetImpulseImpl getImpulse;
//} cpConstraintClass;
//
//struct cpConstraint {
//    const cpConstraintClass *klass;
//
//    cpSpace *space;
//
//    cpBody *a, *b;
//    cpConstraint *next_a, *next_b;
//
//    cpFloat maxForce;
//    cpFloat errorBias;
//    cpFloat maxBias;
//
//    cpBool collideBodies;
//
//    cpConstraintPreSolveFunc preSolve;
//    cpConstraintPostSolveFunc postSolve;
//
//    cpDataPointer userData;
//};
//
//struct cpPinJoint {
//    cpConstraint constraint;
//    cpVect anchorA, anchorB;
//    cpFloat dist;
//
//    cpVect r1, r2;
//    cpVect n;
//    cpFloat nMass;
//
//    cpFloat jnAcc;
//    cpFloat bias;
//};
//
//struct cpSlideJoint {
//    cpConstraint constraint;
//    cpVect anchorA, anchorB;
//    cpFloat min, max;
//
//    cpVect r1, r2;
//    cpVect n;
//    cpFloat nMass;
//
//    cpFloat jnAcc;
//    cpFloat bias;
//};
//
//struct cpPivotJoint {
//    cpConstraint constraint;
//    cpVect anchorA, anchorB;
//
//    cpVect r1, r2;
//    cpMat2x2 k;
//
//    cpVect jAcc;
//    cpVect bias;
//};
//
//struct cpGrooveJoint {
//    cpConstraint constraint;
//    cpVect grv_n, grv_a, grv_b;
//    cpVect  anchorB;
//
//    cpVect grv_tn;
//    cpFloat clamp;
//    cpVect r1, r2;
//    cpMat2x2 k;
//
//    cpVect jAcc;
//    cpVect bias;
//};
//
//struct cpDampedSpring {
//    cpConstraint constraint;
//    cpVect anchorA, anchorB;
//    cpFloat restLength;
//    cpFloat stiffness;
//    cpFloat damping;
//    cpDampedSpringForceFunc springForceFunc;
//
//    cpFloat target_vrn;
//    cpFloat v_coef;
//
//    cpVect r1, r2;
//    cpFloat nMass;
//    cpVect n;
//
//    cpFloat jAcc;
//};
//
//struct cpDampedRotarySpring {
//    cpConstraint constraint;
//    cpFloat restAngle;
//    cpFloat stiffness;
//    cpFloat damping;
//    cpDampedRotarySpringTorqueFunc springTorqueFunc;
//
//    cpFloat target_wrn;
//    cpFloat w_coef;
//
//    cpFloat iSum;
//    cpFloat jAcc;
//};
//
//struct cpRotaryLimitJoint {
//    cpConstraint constraint;
//    cpFloat min, max;
//
//    cpFloat iSum;
//
//    cpFloat bias;
//    cpFloat jAcc;
//};
//
//struct cpRatchetJoint {
//    cpConstraint constraint;
//    cpFloat angle, phase, ratchet;
//
//    cpFloat iSum;
//
//    cpFloat bias;
//    cpFloat jAcc;
//};
//
//struct cpGearJoint {
//    cpConstraint constraint;
//    cpFloat phase, ratio;
//    cpFloat ratio_inv;
//
//    cpFloat iSum;
//
//    cpFloat bias;
//    cpFloat jAcc;
//};
//
//struct cpSimpleMotor {
//    cpConstraint constraint;
//    cpFloat rate;
//
//    cpFloat iSum;
//
//    cpFloat jAcc;
//};
//
//typedef struct cpContactBufferHeader cpContactBufferHeader;
//typedef void (*cpSpaceArbiterApplyImpulseFunc)(cpArbiter *arb);
//
//struct cpSpace {
//    int iterations;
//
//    cpVect gravity;
//    cpFloat damping;
//
//    cpFloat idleSpeedThreshold;
//    cpFloat sleepTimeThreshold;
//
//    cpFloat collisionSlop;
//    cpFloat collisionBias;
//    cpTimestamp collisionPersistence;
//
//    cpDataPointer userData;
//
//    cpTimestamp stamp;
//    cpFloat curr_dt;
//
//    cpArray *dynamicBodies;
//    cpArray *staticBodies;
//    cpArray *rousedBodies;
//    cpArray *sleepingComponents;
//
//    cpHashValue shapeIDCounter;
//    cpSpatialIndex *staticShapes;
//    cpSpatialIndex *dynamicShapes;
//
//    cpArray *constraints;
//
//    cpArray *arbiters;
//    cpContactBufferHeader *contactBuffersHead;
//    cpHashSet *cachedArbiters;
//    cpArray *pooledArbiters;
//
//    cpArray *allocatedBuffers;
//    unsigned int locked;
//
//    cpBool usesWildcards;
//    cpHashSet *collisionHandlers;
//    cpCollisionHandler defaultHandler;
//
//    cpBool skipPostStep;
//    cpArray *postStepCallbacks;
//
//    cpBody *staticBody;
//    cpBody _staticBody;
//};
//
//typedef struct cpPostStepCallback {
//    cpPostStepFunc func;
//    void *key;
//    void *data;
//} cpPostStepCallback;
