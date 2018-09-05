/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   DeadLine.h
 * Author: alex
 *
 * Created on September 4, 2018, 10:39 PM
 */

#ifndef DEADLINE_H
#define DEADLINE_H

#include "../chipmunk/include/chipmunk/chipmunk_structs.h"

#define ASC 0
#define DESC 1

class DeadLine {
public:
    DeadLine(int type, int max_length, int max_height, cpSpace *space);
    DeadLine(const DeadLine& orig);
    virtual ~DeadLine();

    inline void move() {
        cpVect position = mLine->body->p;
        if (mType == ASC) {
            cpBodySetPosition(mLine->body, cpv(position.x, position.y + 0.5));
        } else {
            cpBodySetPosition(mLine->body, cpv(position.x, position.y - 0.5));
        }
    }

    inline cpShape * get_objects_for_space() {
        return mLine;
    }

    inline cpFloat get_position() {
        return mLine->body->p.y;
    }

    int mType;
    cpShape * mLine;
private:

};

#endif /* DEADLINE_H */

