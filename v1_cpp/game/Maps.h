/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Maps.h
 * Author: alex
 *
 * Created on September 4, 2018, 10:39 PM
 */

#ifndef MAPS_H
#define MAPS_H
#include "../chipmunk/include/chipmunk/chipmunk_structs.h"
#include <math.h>
#include <vector>

class SegmentPoint {
public:
    cpVect m_fp, m_sp;
    float m_h;

    SegmentPoint(cpVect fp, cpVect sp, float h) : m_fp(fp), m_sp(sp), m_h(h) {
    };

    ~SegmentPoint() {
    };
};

class ArcPoint {
public:
    cpVect m_c;
    float m_r, m_a, m_b, m_sc;

    ArcPoint(cpVect c, float r, float a, float b, int sc) : m_c(c), m_r(r), m_a(a), m_b(b), m_sc(sc) {
    }

    ~ArcPoint() {
    };
};

class Maps {
public:
    Maps(cpSpace *space);
    Maps(const Maps& orig);

    virtual ~Maps() {
    };

    cpArray * get_cars_start_position() {
        return cars_start_position;
    }


    //    cpArray* get
    //    
    //    def get_objects_for_space(self):
    //        return self.objects
    //    
    //
    //    @classmethod
    //    def get_proto(cls):
    //        return {
    //            'external_id': cls.external_id,
    //            'segments': cls._get_segments_points()
    //        }

    std::vector<cpShape *> getMapShapes(cpSpace *space);

protected:
    virtual std::vector<cpShape *> getBaseSegments(cpSpace *space);
    virtual std::vector<cpShape *> getAdditionalSegments(cpSpace *space);
    virtual std::vector<cpShape *> getBaseArcs(cpSpace *space);
    virtual std::vector<cpShape *> getAdditionalArcs(cpSpace *space);

    std::vector<SegmentPoint> getSegmentsFromArc(cpVect c, float r, float a, float b, int sc);

    cpArray * cars_start_position;
    cpArray * objects;

    virtual cpArray * get_segments_points();
    int external_id;
    float segment_height;

    inline void applyParamsToMapShapes(std::vector<cpShape *> incoming) {
        for (int i = 0; i < incoming.size(); i++) {
            cpShapeSetFriction(incoming[i], segment_friction);
            cpShapeSetElasticity(incoming[i], segment_elasticity);
        }
    };
private:

    int segment_friction;
    int segment_elasticity;
    float max_width;
    float max_height;

    cpShape* left, right, bottom, top;

    void create_box(cpSpace* space);
};

class PillMap : public Maps {
public:

    PillMap(cpSpace *space) : Maps(space) {
        external_id = 1;
    }

    inline std::vector<cpShape*> getBaseSegments(cpSpace *space) override{
        std::vector<cpShape*> target;
        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(300, 100), cpv(900, 100), segment_height));
        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(300, 700), cpv(900, 700), segment_height));
        applyParamsToMapShapes(target);
        return target;
    }

    inline std::vector<cpShape*> getBaseArcs(cpSpace *space) override{
        std::vector<cpShape*> target;
        std::vector<SegmentPoint> temp = getSegmentsFromArc(cpv(300, 400), 300, M_PI / 2, M_PI * 3 / 2, 30);
        int i = 0;
        for (i = 0; i < temp.size(); i++) {
            target.push_back(cpSegmentShapeNew(space->staticBody, temp[i].m_fp, temp[i].m_sp, temp[i].m_h));
        }
        temp = getSegmentsFromArc(cpv(900, 400), 300, M_PI / 2, -M_PI / 2, 30);
        for (i = 0; i < temp.size(); i++) {
            target.push_back(cpSegmentShapeNew(space->staticBody, temp[i].m_fp, temp[i].m_sp, temp[i].m_h));
        }
        applyParamsToMapShapes(target);
        return target;
    }

    virtual ~PillMap() {
    };
};

class PillHubbleMap : public PillMap {
public:

    PillHubbleMap(cpSpace *space) :PillMap(space) {
         external_id = 2;
    }

    inline std::vector<cpShape*> getAdditionalArcs(cpSpace *space) override{
        std::vector<cpShape*> target;
        std::vector<SegmentPoint> temp = getSegmentsFromArc(cpv(600, -150), 300, M_PI / 3.2, M_PI / 1.45, 30);
        int i = 0;
        for (i = 0; i < temp.size(); i++) {
            target.push_back(cpSegmentShapeNew(space->staticBody, temp[i].m_fp, temp[i].m_sp, temp[i].m_h));
        }
        applyParamsToMapShapes(target);
        return target;
    }

    virtual ~PillHubbleMap() {
    };
};

class PillHillMap : public PillMap {
public:

    PillHillMap(cpSpace *space) :PillMap(space) {
         external_id = 3;
    }

    inline std::vector<cpShape*> getAdditionalSegments(cpSpace *space) override{
        std::vector<cpShape*> target;
        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(465, 195), cpv(735, 195), segment_height));

        applyParamsToMapShapes(target);
        return target;
    }

    inline std::vector<cpShape*> getAdditionalArcs(cpSpace *space) override{
        std::vector<cpShape*> target;
        std::vector<SegmentPoint> temp = getSegmentsFromArc(cpv(300, 300), 200, -M_PI / 2, -M_PI / 6, 30);
        int i = 0;
        for (i = 0; i < temp.size(); i++) {
            target.push_back(cpSegmentShapeNew(space->staticBody, temp[i].m_fp, temp[i].m_sp, temp[i].m_h));
        }
        temp = getSegmentsFromArc(cpv(900, 300), 200, M_PI * 3 / 2, M_PI * 7 / 6, 30);
        for (i = 0; i < temp.size(); i++) {
            target.push_back(cpSegmentShapeNew(space->staticBody, temp[i].m_fp, temp[i].m_sp, temp[i].m_h));
        }
        applyParamsToMapShapes(target);
        return target;
    }

    ~PillHillMap() {
    };
};

class PillCarcassMap : public PillMap {
public:

    PillCarcassMap(cpSpace *space) : PillMap(space) {
        external_id = 4;
    }

    inline std::vector<cpShape*> getAdditionalSegments(cpSpace *space) override{
        std::vector<cpShape*> target;
        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(300, 400), cpv(900, 400), segment_height));

        applyParamsToMapShapes(target);
        return target;
    }

    ~PillCarcassMap() {
    }
};

class IslandMap : public Maps {
public:

    IslandMap(cpSpace *space) : Maps(space) {
        external_id = 5;
    }

    inline std::vector<cpShape*> getBaseSegments(cpSpace *space) override{
        std::vector<cpShape*> target;
        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(100, 100), cpv(1100, 100), segment_height));
        applyParamsToMapShapes(target);
        return target;
    }

    ~IslandMap() {
    }
};

class IslandHoleMap : public Maps {
public:

    IslandHoleMap(cpSpace *space) : Maps(space) {
        external_id = 6;
    }

    inline std::vector<cpShape*> getBaseSegments(cpSpace *space) override {
        std::vector<cpShape*> target;
        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(10, 400), cpv(50, 200), segment_height));

        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(50, 200), cpv(300, 200), segment_height));
        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(380, 150), cpv(820, 150), segment_height));
        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(900, 200), cpv(1150, 200), segment_height));
        target.push_back(cpSegmentShapeNew(space->staticBody, cpv(1150, 200), cpv(1190, 400), segment_height));

        applyParamsToMapShapes(target);
        return target;
    }

    inline std::vector<cpShape*> getBaseArcs(cpSpace *space) override {
        std::vector<cpShape*> target;
        std::vector<SegmentPoint> temp = getSegmentsFromArc(cpv(300, 100), 100, M_PI / 6, M_PI / 2, 30);
        int i = 0;
        for (i = 0; i < temp.size(); i++) {
            target.push_back(cpSegmentShapeNew(space->staticBody, temp[i].m_fp, temp[i].m_sp, temp[i].m_h));
        }
        temp = getSegmentsFromArc(cpv(900, 100), 100, M_PI / 2, M_PI * 5 / 6, 30);
        for (i = 0; i < temp.size(); i++) {
            target.push_back(cpSegmentShapeNew(space->staticBody, temp[i].m_fp, temp[i].m_sp, temp[i].m_h));
        }
        applyParamsToMapShapes(target);
        return target;
    }

    ~IslandHoleMap() {
    }
};
#endif /* MAPS_H */

