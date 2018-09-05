/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Maps.cpp
 * Author: alex
 * 
 * Created on September 4, 2018, 10:39 PM
 */

#include "Maps.h"

Maps::Maps(cpSpace *space) {
    external_id = 0;

    segment_friction = 1;
    segment_elasticity = 0;
    segment_height = 10;

    max_width = 1200f;
    max_height = 800f;

    create_box(space);
}

Maps::Maps(const Maps& orig) {
}

Maps::~Maps() {
}

std::vector<cpShape *> Maps::getMapShapes(cpSpace *space) {
    std::vector<cpShape* > target;
    target.push_back(left);
    target.push_back(right);
    target.push_back(top);
    target.push_back(bottom);
    std::vector<cpShape* > tmp = getBaseSegments(space);
    if (!tmp.empty()) {
        std::iterator<cpShape*> it = tmp.begin();
        for (it; it != tmp.end(); it++)
            target.push_back(it);
    }
    tmp = getBaseArcs(space);
    if (!tmp.empty()) {
        std::iterator<cpShape*> it = tmp.begin();
        for (it; it != tmp.end(); it++)
            target.push_back(it);
    }
    tmp = getAdditionalSegments(space);
    if (!tmp.empty()) {
        std::iterator<cpShape*> it = tmp.begin();
        for (it; it != tmp.end(); it++)
            target.push_back(it);
    }
    tmp = getAdditionalArcs(space);
    if (!tmp.empty()) {
        std::iterator<cpShape*> it = tmp.begin();
        for (it; it != tmp.end(); it++)
            target.push_back(it);
    }
    return target;
}

std::vector<SegmentPoint> Maps::getSegmentsFromArc(cpVect c, float r, float a, float b, int sc) {
    float rad_pre_seg = (b - a) / sc;
    std::vector<SegmentPoint> points;
    for (int j = 0; j < sc; j++) {
        float fpoint_rad = a + rad_pre_seg * j;
        float spoint_rad = a + rad_pre_seg * (j + 1);
        cpVect fpoint = cpv(c) + cpv(r * cos(fpoint_rad), r * sin(fpoint_rad));
        cpVect spoint = cpv(c) + cpv(r * cos(spoint_rad), r * sin(spoint_rad));
        points.push_back(SegmentPoint(fpoint, spoint, segment_height));
    }
    return points;
}

void Maps::create_box(cpSpace* space) {
    left = cpSegmentShapeNew(space->staticBody, cpv(0.0, 0.0), cpv(0.0, max_height), 1);
    left->sensor = true;

    top = cpSegmentShapeNew(space->staticBody, cpv(0, max_height), cpv(max_width, max_height), 1);
    top->sensor = true

    right = cpSegmentShapeNew(space->staticBody, cpv(max_width, max_height), cpv(max_width, 0), 1);
    right->sensor = true;

    bottom = cpSegmentShapeNew(space->staticBody, cpv(max_width, 0), cpv(0, 0), 1);
    bottom->sensor = true;

    //self.objects.extend([left, top, right, bottom])
}

std::vector<cpShape *> Maps::getBaseSegments(cpSpace *space) {
}

std::vector<cpShape *> Maps::getAdditionalSegments(cpSpace *space) {
}

std::vector<cpShape *> Maps::getBaseArcs(cpSpace * space) {
}

std::vector<cpShape *> Maps::getAdditionalArcs(cpSpace *space) {
}

//cpArray* Maps::get_segments_points() {
//
//    cpArray *points = cpArrayNew(base_segments.num + additional_segments.num + base_arcs.num + additional_arcs.num);
//    int i = 0;
//    for (; i < base_segments.num; i++) {
//        points[i] = base_segments[i];
//    }
//    int max = i;
//    for (i = 0; i < additional_segments.num; i++) {
//        points[i + max] = additional_segments[i];
//    }
//    max = max + i;
//    for (i = 0; i < base_arcs->num; i++) {
//        float rad_pre_seg = (b - a) / sc;
//        for (int j = 0; j < sc; j++) {
//            float fpoint_rad = a + rad_pre_seg * j;
//            float spoint_rad = a + rad_pre_seg * (j + 1);
//            cpVec fpoint = cpv(c) + cpv(r * cos(fpoint_rad), r * sin(fpoint_rad));
//            cpVec spoint = cpv(c) + cpv(r * cos(spoint_rad), r * sin(spoint_rad));
//            points[i + j + max] = SegmentPoint(fpoint, spoint, segment_height);
//        }
//    }
//    max = max + i;
//    for (i = 0; i < additional_arcs->num; i++) {
//        float rad_pre_seg = (b - a) / sc;
//        for (int j = 0; j < sc; j++) {
//            float fpoint_rad = a + rad_pre_seg * j;
//            float spoint_rad = a + rad_pre_seg * (j + 1);
//            cpVec fpoint = cpv(c) + cpv(r * cos(fpoint_rad), r * sin(fpoint_rad));
//            cpVec spoint = cpv(c) + cpv(r * cos(spoint_rad), r * sin(spoint_rad));
//            points[i + j + max] = SegmentPoint(fpoint, spoint, segment_height);
//        }
//    }
//    return points
//}
