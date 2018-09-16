package gameobjects

import chipmunk_bind.cpGroup
import chipmunk_bind.cpSpace
import chipmunk_bind.cpVect

class Game {

    var space: cpSpace = cpSpace()
    var current_match: Match? = null
    var tick_num = 0

//    val buggy = Buggy()

//    cars_objects.addAll(buggy.get_objects_for_space_at(cpVect(300f, 300f)))

    fun begin() {
        space.gravity = cpVect(0.0f, -700f)
        space.damping = 0.85f
//        buggy.setup(cpGroup(1), BaseCar.RIGHT_DIRECTION, space)

    }

    fun clear_space() {
//        space.remove(space.shapes)
//        space.remove(space.bodies)
//        space.remove(space.constraints)
    }


    fun next_match() {
//    map, car = next(self.matches)
//    clear_space()
        val match = Match(PillHillMap(space),/*buggy, self.all_players,*/ space)
        space.add(match.get_objects_for_space().toTypedArray())
        current_match = match
    }

    fun tick() {
        current_match?.tick(tick_num)
        space.step(0.016f)

//        if self.current_match.is_match_ended():
//        self.game_log.extend(self.current_match.end_match())

//        if not all ([p.is_alive() for p in self.all_players]):
//        self.game_log.append({
//            'type': "end_game",
//            "params": { p.id: p.get_lives() for p in self.all_players }
//        })
//        self.end_game()

//        return 'end_game'
//        self.next_match()

        tick_num += 1
    }
}