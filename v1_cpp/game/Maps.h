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

class SegmentPoint{
public:
    cpVect m_fp,m_sp;
    float m_h;
    SegmentPoint(cpVect fp, cpVect sp, float h):m_fp(fp), m_sp(sp), m_h(h){};
    ~SegmentPoint();
};


class Maps {
public:
    Maps(cpSpace *space);
    Maps(const Maps& orig);
    virtual ~Maps();
protected:
    cpArray * base_arcs, base_segments, additional_arcs, additional_segments, cars_start_position;
    cpArray * objects;

    virtual cpArray * get_segments_points();
private:
    int external_id;
    int segment_friction;
    int segment_elasticity;
    int segment_height;
    float max_width;
    float max_height;

    cpShape* left, right, bottom, top;

    void create_box(cpSpace* space);
};

#endif /* MAPS_H */

