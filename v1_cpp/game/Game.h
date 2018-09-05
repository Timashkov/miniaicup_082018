/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Game.h
 * Author: timashkov
 *
 * Created on September 4, 2018, 1:51 PM
 */

#ifndef GAME_H
#define GAME_H
#include "../chipmunk/include/chipmunk/chipmunk_structs.h"
#include <vector>
#include "Maps.h"

class LogStruct{};

class Game {
public:
    Game();
    Game(const Game& orig);
    virtual ~Game();
    void start();
private:
    void next_match();
    
    
    void clear_space();
    cpSpace * space;
    bool game_complete;
    bool extended_save;
    int max_match_count;
    int tick_num;
    std::vector<LogStruct> game_log;
    int scores[2];
};

#endif /* GAME_H */

