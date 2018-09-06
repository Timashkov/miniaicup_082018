/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   BaseCar.h
 * Author: alex
 *
 * Created on September 4, 2018, 10:35 PM
 */

#ifndef BASECAR_H
#define BASECAR_H

#define FF 1
#define FR 2
#define AWD 3

#define RIGHT_DIRECTION 0
#define LEFT_DIRECTION 1

#include "../chipmunk/include/chipmunk/chipmunk_structs.h"
#include <vector>

class BaseCar {
public:
    BaseCar(cpGroup * car_group, int direction, cpShape* (*foo)(cpVect, int, cpShapeFilter));
    BaseCar(const BaseCar& orig);
    virtual ~BaseCar();
    
    int external_id = 0;

   

    std::vector<cpVect> car_body_poly;
    int car_body_mass = 100;
    cpFloat car_body_friction = 0.9;
    cpFloat car_body_elasticity = 0.5;

    cpVect button_hw = cpv(3, 30);
    cpVect button_position = cpv(0, 0);
    int button_angle = 0;

    int max_speed = 300;
    int max_angular_speed = 2;
    int torque = 20000000;
    int drive = FR;

    int rear_wheel_mass = 60;
    int rear_wheel_radius = 10;
    cpVect rear_wheel_position = cpv(0, 0);
    int rear_wheel_friction = 1;
    cpFloat rear_wheel_elasticity = 0.8;
    cpVect rear_wheel_joint = cpv(0, 0);
    cpVect rear_wheel_damp_position = cpv(0, 0);
    int rear_wheel_damp_length = 20;
    cpFloat rear_wheel_damp_stiffness = 6e4;
    cpFloat rear_wheel_damp_damping = 1e3;

    int front_wheel_mass = 60;
    int front_wheel_radius = 10;
    cpVect front_wheel_position = cpv(0, 0);
    int front_wheel_friction = 1;
    cpFloat front_wheel_elasticity = 0.8;
    cpVect front_wheel_joint = cpv(0, 0);
    cpVect front_wheel_damp_position = cpv(0, 0);
    int front_wheel_damp_length = 20;
    cpFloat front_wheel_damp_stiffness = 6e4;
    cpFloat front_wheel_damp_damping = 0.9e3;
    
    cpGroup * mCarGroup;
    int mXmodification;
    
    
//    cpBody create_car_body() {
//            return cpBodyNew(car_body_mass, pymunk.moment_for_poly(car_body_mass, processed_car_body_poly()));
//    }
private:

    std::vector<cpVect> processed_car_body_poly();
};

#endif /* BASECAR_H */

