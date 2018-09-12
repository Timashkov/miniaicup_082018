package gameobjects

import chipmunk_bind.cpSpace
import chipmunk_bind.cpVect

class Game {

    var space: cpSpace = cpSpace()

    fun begin() {
        space.gravity = cpVect(0.0f, -700f)
        space.damping = 0.85f
    }

    fun clear_space() {
//        space.remove(space.shapes)
//        space.remove(space.bodies)
//        space.remove(space.constraints)
    }


    fun next_match() {
//    map, car = next(self.matches)
//    clear_space()
        val match = Match(PillHillMap(space), Buggy(),/* self.all_players,*/ space)
        space.add(match.get_objects_for_space().toTypedArray())
//    current_match = match
    }
}