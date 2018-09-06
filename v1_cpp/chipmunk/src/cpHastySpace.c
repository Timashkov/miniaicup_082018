// Copyright 2013 Howling Moon Software. All rights reserved.
// See http://chipmunk2d.net/legal.php for more information.

#include <stdlib.h>
#include <stdio.h>

#include <pthread.h>
#include <sys/sysctl.h>

#include "../include/chipmunk/chipmunk_private.h"
#include "../include/chipmunk/cpHastySpace.h"


//MARK: PThreads

// Right now using more than 2 threads probably wont help your performance any.
// If you are using a ridiculous number of iterations it could help though.
#define MAX_THREADS 2

struct ThreadContext {
    pthread_t thread;
    cpHastySpace *space;
    unsigned long thread_num;
};

typedef void (*cpHastySpaceWorkFunction)(cpSpace *space, unsigned long worker, unsigned long worker_count);

struct cpHastySpace {
    cpSpace space;

    // Number of worker threads (including the main thread)
    unsigned long num_threads;

    // Number of worker threads currently executing. (also including the main thread)
    unsigned long num_working;

    // Number of constraints (plus contacts) that must exist per step to start the worker threads.
    unsigned long constraint_count_threshold;

    pthread_mutex_t mutex;
    pthread_cond_t cond_work, cond_resume;

    // Work function to invoke.
    cpHastySpaceWorkFunction work;

    struct ThreadContext workers[MAX_THREADS - 1];
};

static void *
WorkerThreadLoop(struct ThreadContext *context) {
    cpHastySpace *hasty = context->space;

    unsigned long thread = context->thread_num;
    unsigned long num_threads = hasty->num_threads;

    for (;;) {
        pthread_mutex_lock(&hasty->mutex);
        {
            if (--hasty->num_working == 0) {
                pthread_cond_signal(&hasty->cond_resume);
            }

            pthread_cond_wait(&hasty->cond_work, &hasty->mutex);
        }
        pthread_mutex_unlock(&hasty->mutex);

        cpHastySpaceWorkFunction func = hasty->work;
        if (func) {
            hasty->work(&hasty->space, thread, num_threads);
        } else {
            break;
        }
    }

    return NULL;
}

static void
RunWorkers(cpHastySpace *hasty, cpHastySpaceWorkFunction func) {
    hasty->num_working = hasty->num_threads - 1;
    hasty->work = func;

    if (hasty->num_working > 0) {
        pthread_mutex_lock(&hasty->mutex);
        {
            pthread_cond_broadcast(&hasty->cond_work);
        }
        pthread_mutex_unlock(&hasty->mutex);

        func((cpSpace *) hasty, 0, hasty->num_threads);

        pthread_mutex_lock(&hasty->mutex);
        {
            if (hasty->num_working > 0) {
                pthread_cond_wait(&hasty->cond_resume, &hasty->mutex);
            }
        }
        pthread_mutex_unlock(&hasty->mutex);
    } else {
        func((cpSpace *) hasty, 0, hasty->num_threads);
    }

    hasty->work = NULL;
}

static void
Solver(cpSpace *space, unsigned long worker, unsigned long worker_count) {
    cpArray *constraints = space->constraints;
    cpArray *arbiters = space->arbiters;

    cpFloat dt = space->curr_dt;
    unsigned long iterations = (space->iterations + worker_count - 1) / worker_count;

    for (unsigned long i = 0; i < iterations; i++) {
        for (int j = 0; j < arbiters->num; j++) {
            cpArbiter *arb = (cpArbiter *) arbiters->arr[j];
            cpArbiterApplyImpulse(arb);
        }

        for (int j = 0; j < constraints->num; j++) {
            cpConstraint *constraint = (cpConstraint *) constraints->arr[j];
            constraint->klass->applyImpulse(constraint, dt);
        }
    }
}

//MARK: Thread Management Functions

static void
HaltThreads(cpHastySpace *hasty) {
    pthread_mutex_t *mutex = &hasty->mutex;
    pthread_mutex_lock(mutex);
    {
        hasty->work = NULL; // NULL work function means break and exit
        pthread_cond_broadcast(&hasty->cond_work);
    }
    pthread_mutex_unlock(mutex);

    for (unsigned long i = 0; i < (hasty->num_threads - 1); i++) {
        pthread_join(hasty->workers[i].thread, NULL);
    }
}

void
cpHastySpaceSetThreads(cpSpace *space, unsigned long threads) {
    cpHastySpace *hasty = (cpHastySpace *) space;
    HaltThreads(hasty);

    if (threads == 0) threads = 1;

    hasty->num_threads = (threads < MAX_THREADS ? threads : MAX_THREADS);
    hasty->num_working = hasty->num_threads - 1;

    // Create the worker threads and wait for them to signal ready.
    if (hasty->num_working > 0) {
        pthread_mutex_lock(&hasty->mutex);
        for (unsigned long i = 0; i < (hasty->num_threads - 1); i++) {
            hasty->workers[i].space = hasty;
            hasty->workers[i].thread_num = i + 1;

            pthread_create(&hasty->workers[i].thread, NULL, (void * (*)(void*)) WorkerThreadLoop, &hasty->workers[i]);
        }

        pthread_cond_wait(&hasty->cond_resume, &hasty->mutex);
        pthread_mutex_unlock(&hasty->mutex);
    }
}

unsigned long
cpHastySpaceGetThreads(cpSpace *space) {
    return ((cpHastySpace *) space)->num_threads;
}

//MARK: Overriden cpSpace Functions.

cpSpace *
cpHastySpaceNew(void) {
    cpHastySpace *hasty = (cpHastySpace *) cpcalloc(1, sizeof (cpHastySpace));
    cpSpaceInit((cpSpace *) hasty);

    pthread_mutex_init(&hasty->mutex, NULL);
    pthread_cond_init(&hasty->cond_work, NULL);
    pthread_cond_init(&hasty->cond_resume, NULL);

    // TODO magic number, should test this more thoroughly.
    hasty->constraint_count_threshold = 50;

    // Default to 1 thread for determinism.
    hasty->num_threads = 1;
    cpHastySpaceSetThreads((cpSpace *) hasty, 1);

    return (cpSpace *) hasty;
}

void
cpHastySpaceFree(cpSpace *space) {
    cpHastySpace *hasty = (cpHastySpace *) space;

    HaltThreads(hasty);

    pthread_mutex_destroy(&hasty->mutex);
    pthread_cond_destroy(&hasty->cond_work);
    pthread_cond_destroy(&hasty->cond_resume);

    cpSpaceFree(space);
}

void
cpHastySpaceStep(cpSpace *space, cpFloat dt) {
    // don't step if the timestep is 0!
    if (dt == 0.0f) return;

    space->stamp++;

    cpFloat prev_dt = space->curr_dt;
    space->curr_dt = dt;

    cpArray *bodies = space->dynamicBodies;
    cpArray *constraints = space->constraints;
    cpArray *arbiters = space->arbiters;

    // Reset and empty the arbiter list.
    for (int i = 0; i < arbiters->num; i++) {
        cpArbiter *arb = (cpArbiter *) arbiters->arr[i];
        arb->state = CP_ARBITER_STATE_NORMAL;

        // If both bodies are awake, unthread the arbiter from the contact graph.
        if (!cpBodyIsSleeping(arb->body_a) && !cpBodyIsSleeping(arb->body_b)) {
            cpArbiterUnthread(arb);
        }
    }
    arbiters->num = 0;

    cpSpaceLock(space);
    {
        // Integrate positions
        for (int i = 0; i < bodies->num; i++) {
            cpBody *body = (cpBody *) bodies->arr[i];
            body->position_func(body, dt);
        }

        // Find colliding pairs.
        cpSpacePushFreshContactBuffer(space);
        cpSpatialIndexEach(space->dynamicShapes, (cpSpatialIndexIteratorFunc) cpShapeUpdateFunc, NULL);
        cpSpatialIndexReindexQuery(space->dynamicShapes, (cpSpatialIndexQueryFunc) cpSpaceCollideShapes, space);
    }
    cpSpaceUnlock(space, cpFalse);

    // Rebuild the contact graph (and detect sleeping components if sleeping is enabled)
    cpSpaceProcessComponents(space, dt);

    cpSpaceLock(space);
    {
        // Clear out old cached arbiters and call separate callbacks
        cpHashSetFilter(space->cachedArbiters, (cpHashSetFilterFunc) cpSpaceArbiterSetFilter, space);

        // Prestep the arbiters and constraints.
        cpFloat slop = space->collisionSlop;
        cpFloat biasCoef = 1.0f - cpfpow(space->collisionBias, dt);
        for (int i = 0; i < arbiters->num; i++) {
            cpArbiterPreStep((cpArbiter *) arbiters->arr[i], dt, slop, biasCoef);
        }

        for (int i = 0; i < constraints->num; i++) {
            cpConstraint *constraint = (cpConstraint *) constraints->arr[i];

            cpConstraintPreSolveFunc preSolve = constraint->preSolve;
            if (preSolve) preSolve(constraint, space);

            constraint->klass->preStep(constraint, dt);
        }

        // Integrate velocities.
        cpFloat damping = cpfpow(space->damping, dt);
        cpVect gravity = space->gravity;
        for (int i = 0; i < bodies->num; i++) {
            cpBody *body = (cpBody *) bodies->arr[i];
            body->velocity_func(body, gravity, damping, dt);
        }

        // Apply cached impulses
        cpFloat dt_coef = (prev_dt == 0.0f ? 0.0f : dt / prev_dt);
        for (int i = 0; i < arbiters->num; i++) {
            cpArbiterApplyCachedImpulse((cpArbiter *) arbiters->arr[i], dt_coef);
        }

        for (int i = 0; i < constraints->num; i++) {
            cpConstraint *constraint = (cpConstraint *) constraints->arr[i];
            constraint->klass->applyCachedImpulse(constraint, dt_coef);
        }

        // Run the impulse solver.
        cpHastySpace *hasty = (cpHastySpace *) space;
        if ((unsigned long) (arbiters->num + constraints->num) > hasty->constraint_count_threshold) {
            RunWorkers(hasty, Solver);
        } else {
            Solver(space, 0, 1);
        }

        // Run the constraint post-solve callbacks
        for (int i = 0; i < constraints->num; i++) {
            cpConstraint *constraint = (cpConstraint *) constraints->arr[i];

            cpConstraintPostSolveFunc postSolve = constraint->postSolve;
            if (postSolve) postSolve(constraint, space);
        }

        // run the post-solve callbacks
        for (int i = 0; i < arbiters->num; i++) {
            cpArbiter *arb = (cpArbiter *) arbiters->arr[i];

            cpCollisionHandler *handler = arb->handler;
            handler->postSolveFunc(arb, space, handler->userData);
        }
    }
    cpSpaceUnlock(space, cpTrue);
}
