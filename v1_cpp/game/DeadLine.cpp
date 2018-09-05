/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   DeadLine.cpp
 * Author: alex
 * 
 * Created on September 4, 2018, 10:39 PM
 */

#include "DeadLine.h"

DeadLine::DeadLine(int type, int max_length, int max_height, cpSpace *space) {
    
    mType = type;
    cpVect fp = cpv(0, 0);
    cpVect sp = cpv(max_length, 0);
    cpBody * lineBody = cpBodyNewKinematic(); //self.line_body = pymunk.Body(0, 0, pymunk.Body.KINEMATIC)
    mLine = cpSegmentShapeNew(lineBody, fp, sp, 2);
    mLine->sensor = true;
    cpBodySetPosition(mLine->body, cpv(0, mType == ASC ? 10 : max_height - 10));
}

DeadLine::DeadLine(const DeadLine& orig) {
}

DeadLine::~DeadLine() {
}
