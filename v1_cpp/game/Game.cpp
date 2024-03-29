/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Game.cpp
 * Author: timashkov
 * 
 * Created on September 4, 2018, 1:51 PM
 */

#include "Game.h"

Game::Game() {
    game_complete = false;
    extended_save = true;
    max_match_count = 21; // some

    //        self.all_players = [Player(index + 1, client, self.max_match_count) for index, client in enumerate(clients)]

    space = cpSpaceNew();
    space->gravity = cpv(0.0, -700.0);
    space->damping = 0.85;
    scores[0] = 0;
    scores[1] = 0;
    
//        self.matches = self.parse_games(games_list)
//        self.current_match = None
    tick_num = 0;
    
    
    
}

Game::Game(const Game& orig) {
}

Game::~Game() {
    delete space;
}

void Game::start(){
    next_match();    
}

void Game::clear_space(){
    delete(space->staticShapes);
    delete(space->staticBodies);
    delete(space->constraints);
//    space.remove(space.shapes)
//    space.remove(space.bodies)
//    space.remove(space.constraints)
}

void Game::next_match(){
    //    def next_match(self):
//        map, car = next(self.matches)
        clear_space();
        Maps *map = new PillHillMap(space);
        map->getMapShapes(space);
//        match = Match(map, car, self.all_players, self.space)
//        self.space.add(match.get_objects_for_space())
//        self.current_match = match
}

//import asyncio
//import gzip
//import json
//import random
//from collections import defaultdict
//
//import math
//import os
//from itertools import product
//
//import pymunkoptions
//
//from mechanic.constants import MAX_TICK_COUNT
//
//pymunkoptions.options["debug"] = False
//import pymunk
//
//from mechanic.game_objects.cars import Buggy, Bus, SquareWheelsBuggy
//from mechanic.game_objects.maps import PillMap, PillHubbleMap, PillHillMap, PillCarcassMap, IslandMap, IslandHoleMap
//from mechanic.match import Match
//from mechanic.player import Player
//
//
//class Game(object):
//    CARS_MAP = {
//        'Buggy': Buggy,
//        'Bus': Bus,
//        'SquareWheelsBuggy': SquareWheelsBuggy,
//
//    }
//
//    MAPS_MAP = {
//        'PillMap': PillMap,
//        'PillHubbleMap': PillHubbleMap,
//        'PillHillMap': PillHillMap,
//        'PillCarcassMap': PillCarcassMap,
//        'IslandMap': IslandMap,
//        'IslandHoleMap': IslandHoleMap,
//    }
//
//    RESULT_LOCATION = os.environ.get('GAME_LOG_LOCATION', './result')
//
//    BASE_DIR = os.path.dirname(RESULT_LOCATION)
//
//    VISIO_LOCATION = os.path.join(BASE_DIR, 'visio.gz')
//    SCORES_LOCATION = os.path.join(BASE_DIR, 'scores.json')
//    DEBUG_LOCATION = os.path.join(BASE_DIR, '{}')
//
//   
//
//    @classmethod
//    def parse_games(cls, games_list):
//        for g in games_list:
//            m, c = g.split(',', maxsplit=1)
//            yield (cls.MAPS_MAP.get(m, PillMap), cls.CARS_MAP.get(c, Buggy))
//
//   
//    def end_game(self):
//        self.game_complete = True
//        self.game_save()
//
//    def get_winner(self):
//        winner = sorted(self.all_players, key=lambda x: x.lives, reverse=True)
//        if winner:
//            return winner[0]
//        return False
//

//
//    @asyncio.coroutine
//    def game_loop(self):
//        for i in range(MAX_TICK_COUNT):
//            if i % 2000 == 0:
//                print('tick {}'.format(i))
//            is_game_continue = yield from self.tick()
//            if is_game_continue == 'end_game':
//                break
//
//    @asyncio.coroutine
//    def tick(self):
//        yield from self.current_match.tick(self.tick_num)
//        self.space.step(0.016)
//
//        if self.current_match.smbd_die():
//            self.game_log.extend(self.current_match.end_match())
//
//            if not all([p.is_alive() for p in self.all_players]):
//                self.game_log.append({
//                    'type': "end_game",
//                    "params": {p.id: p.get_lives() for p in self.all_players}
//                })
//                self.end_game()
//
//                return 'end_game'
//            self.next_match()
//
//        self.tick_num += 1
//
//    def draw(self, draw_options):
//        self.space.debug_draw(draw_options)
//
//    def get_players_external_id(self):
//        return {p.id: p.get_solution_id() for p in self.all_players}
//
//    def save_visio(self):
//        d = {
//            'config': self.get_players_external_id(),
//            'visio_info': self.game_log
//        }
//        with gzip.open(self.VISIO_LOCATION, 'wb') as f:
//            f.write(json.dumps(d).encode())
//        return {
//            "filename": os.path.basename(self.VISIO_LOCATION),
//            "location": self.VISIO_LOCATION,
//            "is_private": False
//        }
//
//    def save_scores(self):
//        d = {p.get_solution_id(): p.get_lives() for p in self.all_players}
//
//        with open(self.SCORES_LOCATION, 'w') as f:
//            f.write(json.dumps(d))
//
//        return {
//            "filename": os.path.basename(self.SCORES_LOCATION),
//            "location": self.SCORES_LOCATION,
//            "is_private": False
//        }
//
//    def save_debug(self):
//        return [
//            p.save_log(self.DEBUG_LOCATION) for p in self.all_players
//        ]
//
//    def game_save(self):
//        if self.extended_save:
//            result = {
//                "scores": self.save_scores(),
//                "debug": self.save_debug(),
//                "visio": self.save_visio()
//            }
//
//            with open(self.RESULT_LOCATION, 'w') as f:
//                f.write(json.dumps(result))
//        else:
//            self.save_debug()
//            self.save_visio()
//
//    @classmethod
//    def generate_matches(cls, count):
//        available_matches = product(sorted(cls.MAPS_MAP.keys()), sorted(cls.CARS_MAP.keys()))
//        return random.sample([','.join(x) for x in available_matches], count)
