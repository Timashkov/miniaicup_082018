package chipmunk_bind

interface IPointQueryListener {
    fun point_query_nearest(point: cpVect, max_distance: Float, shape_filter: cpShapeFilter): cpPointQueryInfo?
}

val constraintListGlobal = ArrayList<cpConstraint>()

class cpSpace() : IPointQueryListener {

    var iterations: Int = 10

    var gravity = cpVect.cpvzero
    var damping: Float = 1.0f

    var idleSpeedThreshold: Float = 0f
    var sleepTimeThreshold: Float = INFINITY

    var collisionSlop: Float = 0.1f
    var collisionBias: Float = Math.pow(1.0 - 0.1, 60.0).toFloat()
    var collisionPersistence: Long = 3 //cpTimestamp == unsigned int

    var userData: Any? = null

    var curr_dt: Float = 0f

    var allocatedBuffers: ArrayList<Any> = ArrayList()

    var dynamicBodies: ArrayList<cpBody> = ArrayList()
    var staticBodies: ArrayList<cpBody> = ArrayList()
    var rousedBodies: ArrayList<cpBody> = ArrayList()
    var sleepingComponents: ArrayList<cpBody> = ArrayList()

    var shapeIDCounter: Int = 0 //cpHashValue

    var staticShapes: cpSpatialIndex = cpBBTree(object : IcpSpatialIndexBBFunc {
        override fun perform(obj: Any): cpBB {
            return (obj as cpShape).bb
        }

    }, null)
    var dynamicShapes: cpSpatialIndex = cpBBTree(object : IcpSpatialIndexBBFunc {
        override fun perform(obj: Any): cpBB {
            return (obj as cpShape).bb
        }

    }, staticShapes)

    var constraints: ArrayList<cpConstraint> = ArrayList()

    //    space->arbiters = cpArrayNew(0);
//    space->pooledArbiters = cpArrayNew(0);
    var arbiters: ArrayList<cpArbiter> = ArrayList()
    var pooledArbiters: Array<Any> = emptyArray()


//    space->contactBuffersHead = NULL;
//	space->cachedArbiters = cpHashSetNew(0, (cpHashSetEqlFunc)arbiterSetEql);
//

//    cpContactBufferHeader *contactBuffersHead;
//    cpHashSet *cachedArbiters;


    var locked: Int = 0
    var stamp: Int = 0 //cpTimestamp == unsigned int

    var usesWildcards: Boolean = false
    //??
//    cpHashSet *collisionHandlers;
    var defaultHandler: cpCollisionHandler = cpCollisionHandler(
            cpCollisionType(CP_WILDCARD_COLLISION_TYPE), cpCollisionType(CP_WILDCARD_COLLISION_TYPE),
            object : ICPCollisionBeginFunc {
                override fun perform(arb: cpArbiter, space: cpSpace, userData: Any?): Boolean {
                    return true
                }
            }, object : ICPCollisionPreSolveFunc {
        override fun perform(arb: cpArbiter, space: cpSpace, userData: Any?): Boolean {
            return true
        }
    }, object : ICPCollisionPostSolveFunc {
        override fun perform(arb: cpArbiter, space: cpSpace, userData: Any?) {

        }
    }, object : ICPCollisionSeparateFunc {
        override fun perform(arb: cpArbiter, space: cpSpace, userData: Any?) {

        }
    }, null)

    //    space->postStepCallbacks = cpArrayNew(0);
//    space->skipPostStep = cpFalse;
    var skipPostStep: Boolean = false
    var postStepCallbacks: ArrayList<cpPostStepCallback> = ArrayList()

    var staticBody: cpBody = cpBody()

    var _in_step = false
    var _add_later: ArrayList<cpObject> = ArrayList()
    var _remove_later: ArrayList<cpObject> = ArrayList()
    var _removed_shapes: ArrayList<cpObject> = ArrayList()

    init {
        (dynamicShapes as cpBBTree).velocityFunc = object : IcpBBTreeVelocityFunc {
            override fun perform(obj: Any?): cpVect {
                return (obj as cpShape).body.v
            }
        }
        staticBody.cpBodyInit(0f, 0f)
        staticBody.cpBodySetType(cpBodyType.CP_BODY_TYPE_STATIC)
        staticBody.space = this


    }

    fun add(objs: Array<cpObject>) {
//        """Add one or many shapes, bodies or joints to the space
//
//        Unlike Chipmunk and earlier versions of pymunk its now allowed to add
//        objects even from a callback during the simulation step. However, the
//        add will not be performed until the end of the step.
//        """

        if (_in_step) {
            _add_later.addAll(objs)
            return
        }

        objs.forEach {

            if (it is cpBody) {
                _add_body(it)
            } else if (it is cpShape) {
                _add_shape(it)
            } else if (it is cpConstraint) {
                _add_constraint(it)
            }
//            else if (it is Array) {
//                add(it)
//            }
        }
    }

    fun remove(objs: Array<cpObject>) {
//    """Remove one or many shapes, bodies or constraints from the space
//
//        Unlike Chipmunk and earlier versions of Pymunk its now allowed to
//        remove objects even from a callback during the simulation step.
//        However, the removal will not be performed until the end of the step.
//
//        .. Note::
//            When removing objects from the space, make sure you remove any
//            other objects that reference it. For instance, when you remove a
//            body, remove the joints and shapes attached to it.
//        """

        if (_in_step) {
            _remove_later.addAll(objs)
            return
        }

        objs.forEach {
            if (it is cpBody) {
                _remove_body(it)
            } else if (it is cpShape) {
                _remove_shape(it)
            } else if (it is cpConstraint) {
                _remove_constraint(it)
            }
        }

//        for o in objs:
//        if isinstance(o, Body):
//        self._remove_body(o)
//        elif isinstance (o, Shape):
//        self._remove_shape(o)
//        elif isinstance (o, Constraint):
//        self._remove_constraint(o)
//        else:
//        for oo in o:
//        self.remove(oo)
    }

    fun _add_shape(shape: cpShape) {

        val isStatic = (shape.body.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_STATIC)
        if (!isStatic) shape.body.cpBodyActivate()
        shape.body.cpBodyAddShape(shape)

        shape.hashid = shapeIDCounter + 1
        shape.cpShapeUpdate(shape.body.transform)
        (if (isStatic) staticShapes else dynamicShapes).insert(shape, shape.hashid)
        shape.space = this
    }

    fun _add_body(body: cpBody) {
        cpSpaceArrayForBodyType(body.cpBodyGetType()).add(body)
        body.space = this
    }

    fun _add_constraint(constraint: cpConstraint) {

        val a = constraint.a
        val b = constraint.b

        a.cpBodyActivate()
        b.cpBodyActivate()

        constraints.add(constraint)
        constraintListGlobal.add(constraint)

        constraint.space = this

        constraintListGlobal.add(constraint)
    }

    fun _remove_shape(shape: cpShape) {
        val body = shape.body

        val isStatic = body.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_STATIC
        if (isStatic) {
            body.cpBodyActivateStatic(shape)
        } else {
            body.cpBodyActivate()
        }

        body.cpBodyRemoveShape(shape)
        cpSpaceFilterArbiters(body, shape)
        (if (isStatic) staticShapes else dynamicShapes).remove(shape, shape.hashid)
        shape.space = null
        shape.hashid = 0
    }

    fun cpSpaceFilterArbiters(body: cpBody, filter: cpShape) {
        cpSpaceLock()
        val context = arbiterFilterContext(this, body, filter)
//        cpHashSetFilter(cachedArbiters, (cpHashSetFilterFunc) cachedArbitersFilter, context)
        cpSpaceUnlock(true)
    }

    fun _remove_body(body: cpBody) {
        body.cpBodyActivate()
        cpSpaceArrayForBodyType(body.cpBodyGetType()).remove(body)
        body.space = null
    }

    fun _remove_constraint(constraint: cpConstraint) {
        constraint.a.cpBodyActivate()
        constraint.b.cpBodyActivate()

        constraintListGlobal.remove(constraint)

        constraint.a.constraintList.remove(constraint)
        constraint.b.constraintList.remove(constraint)
        constraint.space = null
    }

    fun step(dt: Float) {
//        """Update the space for the given time step.
//
//        Using a fixed time step is highly recommended. Doing so will increase
//        the efficiency of the contact persistence, requiring an order of
//        magnitude fewer iterations to resolve the collisions in the usual case.
//
//        It is not the same to call step 10 times with a dt of 0.1 and
//        calling it 100 times with a dt of 0.01 even if the end result is
//        that the simulation moved forward 100 units. Performing  multiple
//        calls with a smaller dt creates a more stable and accurate
//        simulation. Therefor it sometimes make sense to have a little for loop
//        around the step call, like in this example:
//
//        >>> import pymunk
//        >>> s = pymunk.Space()
//        >>> steps = 10
//        >>> for x in range(steps): # move simulation forward 0.1 seconds:
//        ...     s.step(0.1 / steps)
//
//        :param float dt: Time step length
//        """

        _in_step = true

        cpSpaceStep(dt)
        _removed_shapes.clear()
        _in_step = false

        _add_later.forEach {
            add(arrayOf(it))
        }
        _add_later.clear()

        _remove_later.forEach {
            remove(arrayOf(it))
        }

        _remove_later.clear()

//        for key in self._post_step_callbacks {
//            self._post_step_callbacks[key](self)
//        }
//        _post_step_callbacks.clear()
    }

    override fun point_query_nearest(point: cpVect, max_distance: Float, shape_filter: cpShapeFilter): cpPointQueryInfo? {
////        """Query space at point the nearest shape within the given distance
////        range.
////
////        The filter is applied to the query and follows the same rules as the
////        collision detection. If a maxDistance of 0.0 is used, the point must
////        lie inside a shape. Negative max_distance is also allowed meaning that
////        the point must be a under a certain depth within a shape to be
////        considered a match.
////
////        See :py:class:`ShapeFilter` for details about how the shape_filter
////        parameter can be used.
////
////        .. Note::
////            Sensor shapes are not included in the result (In
////            :py:meth:`Space.point_query` they are)
////
////        :param point: Where to check for collision in the Space
////        :type point: :py:class:`~vec2d.Vec2d` or (float,float)
////        :param float max_distance: Match only within this distance
////        :param ShapeFilter shape_filter: Only pick shapes matching the filter
////
////        :rtype: :py:class:`PointQueryInfo` or None
////        """
//
        val out = cpPointQueryInfo(null, cpVect.cpvzero, max_distance, cpVect.cpvzero)

        val context = PointQueryContext(point, max_distance, shape_filter, null)

        val bb = cpBBNewForCircle(point, Math.max(max_distance, 0.0f))
        dynamicShapes.query(context, bb, NearestPointQueryNearest, out)
        staticShapes.query(context, bb, NearestPointQueryNearest, out)

        return if (out.shape == null) null else out
    }

    fun add_wildcard_collision_handler(collision_type_a: cpCollisionType): cpCollisionHandler {
//    """Add a wildcard collision handler for given collision type.
//
//        This handler will be used any time an object with this type collides
//        with another object, regardless of its type. A good example is a
//        projectile that should be destroyed the first time it hits anything.
//        There may be a specific collision handler and two wildcard handlers.
//        It's up to the specific handler to decide if and when to call the
//        wildcard handlers and what to do with their return values.
//
//        When a new wildcard handler is created, the callbacks will all be
//        set to builtin callbacks that perform the default behavior. (accept
//        all collisions in :py:func:`~CollisionHandler.begin` and
//        :py:func:`~CollisionHandler.pre_solve`, or do nothing for
//        :py:func:`~CollisionHandler.post_solve` and
//        :py:func:`~CollisionHandler.separate`.
//
//        :param int collision_type_a: Collision type
//        :rtype: :py:class:`CollisionHandler`
//        """

        cpSpaceUseWildcardDefaultHandler()
        return defaultHandler
//        cpHashValue hash = CP_HASH_PAIR (type, CP_WILDCARD_COLLISION_TYPE);
//        cpCollisionHandler handler = { type, CP_WILDCARD_COLLISION_TYPE, AlwaysCollide, AlwaysCollide, DoNothing, DoNothing, NULL };
//        return (cpCollisionHandler *) cpHashSetInsert (space->collisionHandlers, hash, &handler, (cpHashSetTransFunc)handlerSetTrans, NULL);


    }

    fun cpSpaceActivateBody(body: cpBody) {
//        cpAssertHard(cpBodyGetType(body) == CP_BODY_TYPE_DYNAMIC, "Internal error: Attempting to activate a non-dynamic body.");

        if (locked != 0) {
            // cpSpaceActivateBody() is called again once the space is unlocked
            if (!rousedBodies.contains(body))
                rousedBodies.add(body)
        } else {
//            cpAssertSoft(body->sleeping.root == NULL && body->sleeping.next == NULL, "Internal error: Activating body non-NULL node pointers.");
            dynamicBodies.add(body)
            body.shapeList.forEach {
                staticShapes.remove(it, it.hashid)
                dynamicShapes.insert(it, it.hashid)
            }

            body.arbiterList.forEach { arb ->
                val bodyA = arb.body_a

                // Arbiters are shared between two bodies that are always woken up together.
                // You only want to restore the arbiter once, so bodyA is arbitrarily chosen to own the arbiter.
                // The edge case is when static bodies are involved as the static bodies never actually sleep.
                // If the static body is bodyB then all is good. If the static body is bodyA, that can easily be checked.
                if (body == bodyA || bodyA?.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_STATIC) {
                    val numContacts = arb.count
                    val contacts = arb.contacts

                    // Restore contact values back to the space's contact buffer memory
//                    arb.contacts = cpContactBufferGetArray()
//                    memcpy(arb->contacts, contacts, numContacts*sizeof(struct cpContact));
//                    cpSpacePushContacts(numContacts)


                    // Reinsert the arbiter into the arbiter cache
//                    val a = arb.a
//                    val b = arb.b
//                    val shape_pair [] = { a, b };
//                    val arbHashID = CP_HASH_PAIR((cpHashValue) a, (cpHashValue) b);
//                    cpHashSetInsert(space->cachedArbiters, arbHashID, shape_pair, NULL, arb);

                    // Update the arbiter's state
                    arb.stamp = stamp
                    arbiters.add(arb)

//                    cpfree(contacts);
                }
            }

            body.constraintList.forEach {
                val bodyA = it.a
                if (body == bodyA || bodyA.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_STATIC)
                    constraints.add(it)
            }
        }
    }

    fun cpSpacePushContacts(contact: ArrayList<cpContact>) {
//        cpAssertHard(count <= CP_MAX_CONTACTS_PER_ARBITER, "Internal Error: Contact buffer overflow!");
//        space->contactBuffersHead->numContacts += count;
    }

    fun cpSpacePointQueryNearest(point: cpVect, maxDistance: Float, filter: cpShapeFilter, out: cpPointQueryInfo): cpShape? {
        var out = cpPointQueryInfo(null, cpVect.cpvzero, maxDistance, cpVect.cpvzero)

        val context = PointQueryContext(point, maxDistance, filter, null)

        val bb = cpBBNewForCircle(point, Math.max(maxDistance, 0.0f))


        dynamicShapes.query(context, bb, NearestPointQueryNearest, out)
        staticShapes.query(context, bb, NearestPointQueryNearest, out)

        return out.shape
    }

    fun cpSpaceArrayForBodyType(type: cpBodyType): ArrayList<cpBody> {
        return if (type == cpBodyType.CP_BODY_TYPE_STATIC) staticBodies else dynamicBodies
    }

    fun cpSpaceSetStaticBody(body: cpBody) {
        if (staticBody != null) {
//        cpAssertHard(space->staticBody->shapeList == NULL, "Internal Error: Changing the designated static body while the old one still had shapes attached.");
            staticBody.space = null
        }

        staticBody = body
        body.space = this
    }

    fun cpSpaceLock() {
        locked++
    }

    fun cpSpaceUnlock(runPostStep: Boolean) {
        locked--
        //cpAssertHard(space->locked >= 0, "Internal Error: Space lock underflow.");

        if (locked == 0) {
            val waking = rousedBodies

            while (waking.size > 0) {
                cpSpaceActivateBody(waking[0])
                waking.remove(waking[0])
            }

            if (locked == 0 && runPostStep && !skipPostStep) {
                skipPostStep = true

                val arr = postStepCallbacks
                while (arr.size > 0) {
                    val callback = arr[0]
                    val func = callback.func

                    // Mark the func as NULL in case calling it calls cpSpaceRunPostStepCallbacks() again.
                    // TODO: need more tests around this case I think.
                    callback.func = null
                    func?.perform(this, callback.key, callback.data)

                    arr.remove(arr[0])
                }
                skipPostStep = false
            }
        }
    }

    fun cpSpaceStep(dt: Float) {
        // don't step if the timestep is 0!
        if (dt == 0.0f) return;

        stamp++

        val prev_dt = curr_dt
        curr_dt = dt

        val bodies = dynamicBodies
        val constraints = constraints
        val arbiters = arbiters

        // Reset and empty the arbiter lists.
        arbiters.forEach {
            it.state = cpArbiterState.CP_ARBITER_STATE_NORMAL
            if (it.body_a?.cpBodyIsSleeping(it.body_a!!) == false && it.body_b?.cpBodyIsSleeping(it.body_b!!) == false)
                it.cpArbiterUnthread()
        }

        arbiters.clear()

        cpSpaceLock()

        // Integrate positions
        bodies.forEach {
            it.position_func?.perform(dt)
        }

        // Find colliding pairs.
//        cpSpacePushFreshContactBuffer(space);
//        cpSpatialIndexEach(space->dynamicShapes, (cpSpatialIndexIteratorFunc)cpShapeUpdateFunc, NULL);
//        cpSpatialIndexReindexQuery(space->dynamicShapes, (cpSpatialIndexQueryFunc)cpSpaceCollideShapes, space);

        cpSpaceUnlock(false)

        // Rebuild the contact graph (and detect sleeping components if sleeping is enabled)
        cpSpaceProcessComponents(dt)

        cpSpaceLock()

        // Clear out old cached arbiters and call separate callbacks
//        cpHashSetFilter(space->cachedArbiters, (cpHashSetFilterFunc)cpSpaceArbiterSetFilter, space);

        // Prestep the arbiters and constraints.
        val slop = collisionSlop
        val biasCoef = 1.0f - Math.pow(collisionBias.toDouble(), dt.toDouble()).toFloat()
        arbiters.forEach {
            it.cpArbiterPreStep(dt, slop, biasCoef)
        }

        constraints.forEach { constraint ->
            val preSolve = constraint.preSolve
            if (preSolve != null) preSolve.perform(constraint, this)

            constraint.klass.preStep(constraint, dt)
        }

        // Integrate velocities.
        val doubledamp = damping.toDouble()
        val doubledt = dt.toDouble()
        val damp = Math.pow(doubledamp, doubledt).toFloat()
        val gravity = gravity
        bodies.forEach { body ->
            body.velocity_func?.perform(gravity, damp, dt)
        }


        // Apply cached impulses
        val dt_coef = if (prev_dt == 0.0f) 0.0f else dt / prev_dt
        arbiters.forEach {
            it.cpArbiterApplyCachedImpulse(dt_coef)
        }

        constraints.forEach { constraint ->
            constraint.klass.applyCachedImpulse(constraint, dt_coef)
        }

        // Run the impulse solver.

        for (i in 0 until iterations) {
            arbiters.forEach {
                it.cpArbiterApplyImpulse()
            }
        }

        constraints.forEach { constraint ->
            constraint.klass.applyImpulse(constraint, dt)
        }
        constraints.forEach { constraint ->
            val postSolve = constraint.postSolve
            postSolve?.perform(constraint, this)
        }

        // run the post-solve callbacks
        arbiters.forEach { arb ->
            val handler = arb.handler
            handler?.postSolveFunc?.perform(arb, this, handler.userData)
        }

        cpSpaceUnlock(true)
    }


    fun cpSpaceProcessComponents(dt: Float) {
        val sleep = (sleepTimeThreshold != INFINITY)

        // Calculate the kinetic energy of all the bodies.
        if (sleep) {
            val dv = idleSpeedThreshold
            val dvsq = if (dv != 0f) dv * dv else cpVect.cpvlengthsq(gravity) * dt * dt

            // update idling and reset component nodes
            dynamicBodies.forEach {
                if (it.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_DYNAMIC) {
                    val keThreshold = if (dvsq != 0f) it.m * dvsq else 0.0f
                    it.sleeping.idleTime = if (it.cpBodyKineticEnergy() > keThreshold) 0.0f else it.sleeping.idleTime + dt
                }
            }
        }

        // Awaken any sleeping bodies found and then push arbiters to the bodies' lists.
        arbiters.forEach { arb ->
            val a = arb.body_a!!
            val b = arb.body_b!!

            if (sleep) {
                // TODO checking cpBodyIsSleepin() redundant?
                if (b.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_KINEMATIC || a.cpBodyIsSleeping(a)) a.cpBodyActivate()
                if (a.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_KINEMATIC || b.cpBodyIsSleeping(b)) b.cpBodyActivate()
            }

            a.cpBodyPushArbiter(arb)
            b.cpBodyPushArbiter(arb)

        }


        if (sleep) {
            // Bodies should be held active if connected by a joint to a kinematic.
            constraints.forEach { constraint ->
                val a = constraint.a
                val b = constraint.b

                if (b.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_KINEMATIC) a.cpBodyActivate()
                if (a.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_KINEMATIC) b.cpBodyActivate()

            }

            // Generate components and deactivate sleeping ones
            dynamicBodies.forEach { body ->
                if (body.ComponentRoot() == null) {
                    // Body not in a component yet. Perform a DFS to flood fill mark
                    // the component in the contact graph using this body as the root.
                    body.FloodFillComponent(body)

                    // Check if the component should be put to sleep.
                    if (!body.ComponentActive(sleepTimeThreshold)) {
                        sleepingComponents.add(body)

                        var other: cpBody? = body
                        do {
                            other = other?.sleeping?.next
                            if (other != null) {
                                cpSpaceDeactivateBody(other)
                            }
                        } while (other != null)

                        // cpSpaceDeactivateBody() removed the current body from the list.
                        // Skip incrementing the index counter.
                    }
                } else {

                    // Only sleeping bodies retain their component node pointers.
                    body.sleeping.root = null
                    body.sleeping.next = null
                }
            }
        }
    }

    fun cpSpaceDeactivateBody(body: cpBody) {
//        cpAssertHard(cpBodyGetType(body) == CP_BODY_TYPE_DYNAMIC, "Internal error: Attempting to deactivate a non-dynamic body.");

        dynamicBodies.remove(body)
//        cpArrayDeleteObj(space->dynamicBodies, body);

        body.shapeList.forEach { shape ->
            dynamicShapes.remove(shape, shape.hashid)
            staticShapes.insert(shape, shape.hashid)
        }

        body.arbiterList.forEach { arb ->
            val bodyA = arb.body_a
            if (body == bodyA || bodyA?.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_STATIC) {
                cpSpaceUncacheArbiter(arb)

                // Save contact values to a new block of memory so they won't time out
//                val bytes = arb.count*sizeof(struct cpContact);
//                struct cpContact *contacts = (struct cpContact *)cpcalloc(1, bytes);
//                memcpy(contacts, arb->contacts, bytes);
//                arb->contacts = contacts;
            }
        }

        body.constraintList.forEach { constraint ->
            val bodyA = constraint.a
            if (body == bodyA || bodyA.cpBodyGetType() == cpBodyType.CP_BODY_TYPE_STATIC)
                constraints.remove(constraint)
        }

    }

    fun cpSpaceUncacheArbiter(arb: cpArbiter) {
        val a = arb.a
        val b = arb.b

//        const cpShape *shape_pair[] = {a, b};
//        cpHashValue arbHashID = CP_HASH_PAIR((cpHashValue)a, (cpHashValue)b);
//        cpHashSetRemove(space->cachedArbiters, arbHashID, shape_pair);
//

        arbiters.remove(arb)
    }

    fun cpSpaceUseWildcardDefaultHandler() {
        // Spaces default to using the slightly faster "do nothing" default handler until wildcards are potentially needed.
        if (!usesWildcards) {
            usesWildcards = true

            defaultHandler = cpCollisionHandler(cpCollisionType(CP_WILDCARD_COLLISION_TYPE), cpCollisionType(CP_WILDCARD_COLLISION_TYPE),
                    object : ICPCollisionBeginFunc {
                        override fun perform(arb: cpArbiter, space: cpSpace, userData: Any?): Boolean {
                            val retA = arb.cpArbiterCallWildcardBeginA(space)
                            val retB = arb.cpArbiterCallWildcardBeginB(space)
                            return retA && retB
                        }
                    }, object : ICPCollisionPreSolveFunc {
                override fun perform(arb: cpArbiter, space: cpSpace, userData: Any?): Boolean {
                    val retA = arb.cpArbiterCallWildcardPreSolveA(space)
                    val retB = arb.cpArbiterCallWildcardPreSolveB(space)
                    return retA && retB
                }
            }, object : ICPCollisionPostSolveFunc {
                override fun perform(arb: cpArbiter, space: cpSpace, userData: Any?) {
                    arb.cpArbiterCallWildcardPostSolveA(space)
                    arb.cpArbiterCallWildcardPostSolveB(space)
                }
            }, object : ICPCollisionSeparateFunc {
                override fun perform(arb: cpArbiter, space: cpSpace, userData: Any?) {
                    arb.cpArbiterCallWildcardSeparateA(space)
                    arb.cpArbiterCallWildcardSeparateB(space)

                }
            }, null
            )
        }
    }
}


data class arbiterFilterContext(var space: cpSpace?, var body: cpBody?, var shape: cpShape?)