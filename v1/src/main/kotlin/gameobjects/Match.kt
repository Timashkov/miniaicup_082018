package gameobjects

import chipmunk_bind.cpGroup
import chipmunk_bind.cpObject
import chipmunk_bind.cpSpace
import chipmunk_bind.cpVect

class Match(val map: Map, /*val car: BaseCar, players: Player,*/space: cpSpace) {
    companion object {
        const val TICKS_TO_DEADLINE = 600
    }

    val ticks_to_deadline = TICKS_TO_DEADLINE

    val cars_objects = ArrayList<cpObject>()
    val map_objects = map.get_objects_for_space()

    var is_rest = false
    var rest_counter = 0

    init {

//        self.players = players
        val deadline = DeadLine(DeadLine.ASC, 1800f, 800f, space)

        map_objects.add(deadline.get_object_for_space())
//        self.dead_players = set()

//        self.match_log = []



//        for index, player in enumerate(self.players):
//        if (index + 1) % 2:
//        c = car(player.get_game_id(), Car.RIGHT_DIRECTION, space.point_query_nearest)
//        self.cars_objects.extend(c.get_objects_for_space_at(Vec2d(300, 300)))
//        else:
//        c = car(player.get_game_id(), Car.LEFT_DIRECTION, space.point_query_nearest)
//        self.cars_objects.extend(c.get_objects_for_space_at(Vec2d(900, 300)))

        val c1 = Buggy()
        c1.setup(cpGroup(1), BaseCar.RIGHT_DIRECTION, space)
        cars_objects.addAll(c1.get_objects_for_space_at(cpVect(300f, 300f)))
        val co1 = space.add_wildcard_collision_handler(c1.get_button_collision_type())
//        co1.begin = partial(lose_callback, player)
//        player.set_car(c1)


        val c2 = Buggy()
        c2.setup(cpGroup(2), BaseCar.LEFT_DIRECTION, space)
        cars_objects.addAll(c2.get_objects_for_space_at(cpVect(900f, 300f)))
        val co2 = space.add_wildcard_collision_handler(c2.get_button_collision_type())
//        co2.begin = partial(lose_callback, player)
//        player.set_car(c2)


//        co = space.add_wildcard_collision_handler(c.get_button_collision_type())
//        co.begin = partial(self.lose_callback, player)
//        player.set_car(c)

//        self.send_new_match_message()
    }


    fun get_objects_for_space(): ArrayList<cpObject> {
        val objs = ArrayList<cpObject>()
        objs.addAll(map_objects)
        objs.addAll(cars_objects)
        return objs
    }

    fun tick( game_tick: Int) {
        if (!is_rest /*&& !smbd_die()*/){
            rest_counter =1000000// REST_TICKS
            is_rest = true
        }
        if (rest_counter > 0)
            rest_counter -= 1

//        send_tick(game_tick)
//        futures = []
        // do turn left/right/stop
//        for p in self.players:
//        futures.append(asyncio.ensure_future(self.apply_turn_wrapper(p, game_tick)))
//        if futures:
        //wait for perform
//        yield from asyncio.wait(futures)
//
//        if self.ticks_to_deadline < 1:
//        self.deadline.move()
//        else:
//        self.ticks_to_deadline -= 1
    }
}

/*
class Match:


    @asyncio.coroutine
    def apply_turn_wrapper(self, player, game_tick):
        if not self.is_rest:
            yield from player.apply_turn(game_tick)

    @asyncio.coroutine
    def tick(self, game_tick):
        if not self.is_rest and self.smbd_die():
            self.rest_counter = REST_TICKS
            self.is_rest = True

        if self.rest_counter > 0:
            self.rest_counter -= 1

        self.send_tick(game_tick)
        futures = []
        for p in self.players:
            futures.append(asyncio.ensure_future(self.apply_turn_wrapper(p, game_tick)))
        if futures:
            yield from asyncio.wait(futures)

        if self.ticks_to_deadline < 1:
            self.deadline.move()
        else:
            self.ticks_to_deadline -= 1



    def get_players_lives(self, player=None):
        if player:
            myself = player.lives
            enemy = None
            for p in self.players:
                if player != p:
                    enemy = p.lives
                    break
            return myself, enemy
        else:
            return {p.id: p.lives for p in self.players}

    def get_players_car(self, player=None):
        if player:
            my = player.car.fast_dump()
            enemy = None

            for p in self.players:
                if p != player:
                    enemy = p.car.fast_dump()
                    break
            return my, enemy
        else:
            return {p.id: p.car.fast_dump()for p in self.players}

    def send_new_match_message(self):
        proto_map = self.map.get_proto()
        proto_car = self.car.proto_dump()

        self.match_log.append({
            'type': 'new_match',
            'params': {
                'lives': self.get_players_lives(),
                'proto_map': proto_map,
                'proto_car': proto_car
            }
        })

        for p in self.players:
            my_lives, enemy_lives = self.get_players_lives(p)
            p.send_message('new_match', {
                'my_lives': my_lives,
                'enemy_lives': enemy_lives,
                'proto_map': proto_map,
                'proto_car': proto_car,
            })

    def send_tick(self, game_tick):
        self.match_log.append({
            'type': 'tick',
            'params': {
                'cars': self.get_players_car(),
                'deadline_position': self.deadline.get_position(),
                'tick_num': game_tick
            }
        })

        if not self.is_rest:
            for p in self.players:
                my_car, enemy_car = self.get_players_car(p)
                p.send_message('tick', {
                    'my_car': my_car,
                    'enemy_car': enemy_car,
                    'deadline_position': self.deadline.get_position()
                })

    def lose_callback(self, player, arbiter, space, _):
        if not self.is_rest:
            self.dead_players.add(player)
        return False

    def smbd_die(self):
        return bool(self.dead_players)

    def is_match_ended(self):
        return self.rest_counter == 0 and bool(self.dead_players) and self.is_rest

    def end_match(self):
        for p in self.dead_players:
            p.die()
        return self.match_log
*/