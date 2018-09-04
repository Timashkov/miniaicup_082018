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

        for fp, sp, h in self._get_segments_points():
            segment = pymunk.Segment(space.static_body, fp, sp, h)
            segment.friction = self.segment_friction
            segment.elasticity = self.segment_elasticity
            self.objects.append(segment)
}

Maps::Maps(const Maps& orig) {
}

Maps::~Maps() {
}

void Maps::create_box(cpSpace* space){
        left = cpSegmentShapeNew(space->staticBody, cpv(0f, 0f), cpv(0f, max_height), 1);
        left->sensor = true;

        top = cpSegmentShapeNew(space->staticBody, cpv(0, max_height), cpv(max_width, max_height), 1);
        top->sensor = true

        right = cpSegmentShapeNew(space->staticBody, cpv(max_width, max_height), cpv(max_width, 0), 1);
        right->sensor = true;

        bottom = cpSegmentShapeNew(space->staticBody, cpv(max_width, 0), cpv(0, 0), 1);
        bottom->sensor = true;

        //self.objects.extend([left, top, right, bottom])
}

cpArray* Maps::get_segments_points(){
    
        cpArray *points = cpArrayNew(base_segments.num + additional_segments.num + base_arcs.num + additional_arcs.num);
        int i = 0;
        for (; i < base_segments.num;i++){
            points[i] =  base_segments[i];
        }
        int max = i;
        for (i = 0; i < additional_segments.num;i++){
            points[i+max] =  additional_segments[i];
        }
        
        for c, r, a, b, sc in cls.base_arcs + cls.additional_arcs:
            rad_pre_seg = (b - a) / sc
            for i in range(sc):
                fpoint_rad = a + rad_pre_seg * i
                spoint_rad = a + rad_pre_seg * (i + 1)
                fpoint = Vec2d(c) + Vec2d(r * math.cos(fpoint_rad), r * math.sin(fpoint_rad))
                spoint = Vec2d(c) + Vec2d(r * math.cos(spoint_rad), r * math.sin(spoint_rad))
                points.append([tuple(fpoint), tuple(spoint), cls.segment_height])

        return points
}
                    
import math
import pymunk
from pymunk import Vec2d


class Map(object):
    
    

    

    
    

    def get_objects_for_space(self):
        return self.objects

    def get_cars_start_position(self):
        return self.cars_start_position

    @classmethod
    def get_proto(cls):
        return {
            'external_id': cls.external_id,
            'segments': cls._get_segments_points()
        }


class PillMap(Map):
    external_id = 1

    base_arcs = [
        ((300, 400), 300, math.pi/2, math.pi * 3/2, 30),
        ((900, 400), 300, math.pi/2, -math.pi / 2,  30),
    ]

    base_segments = [
        ((300, 100), (900, 100)),
        ((300, 700), (900, 700))
    ]


class PillHubbleMap(PillMap):
    external_id = 2

    additional_arcs = [
        ((600, -150), 300, math.pi/3.2, math.pi/1.45, 30)
    ]


class PillHillMap(PillMap):
    external_id = 3

    additional_arcs = [
        ((300, 300), 200, -math.pi / 2, -math.pi / 6, 30),
        ((900, 300), 200, math.pi * 3 / 2, math.pi * 7 / 6, 30),
    ]

    additional_segments = [
        ((465, 195), (735, 195))
    ]


class PillCarcassMap(PillMap):
    external_id = 4

    additional_segments = [
        ((300, 400), (900, 400))
    ]


class IslandMap(Map):
    external_id = 5

    base_segments = [
        ((100, 100), (1100, 100)),
    ]


class IslandHoleMap(Map):
    external_id = 6

    base_segments = [
        ((10, 400), (50, 200)),
        ((50, 200), (300, 200)),

        ((380, 150), (820, 150)),
        ((900, 200), (1150, 200)),
        ((1150, 200), (1190, 400))
    ]

    base_arcs = [
        ((300, 100), 100, math.pi / 6, math.pi / 2, 30),
        ((900, 100), 100, math.pi / 2, math.pi * 5 / 6, 30),
    ]