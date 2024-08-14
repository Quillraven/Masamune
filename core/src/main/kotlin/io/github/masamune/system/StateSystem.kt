package io.github.masamune.system

import State
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class StateSystem : IteratingSystem(family { all(State) }) {
    override fun onTickEntity(entity: Entity) {
        entity[State].animationFsm.update()
    }
}
