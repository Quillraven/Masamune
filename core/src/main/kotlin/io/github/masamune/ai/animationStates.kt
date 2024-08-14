package io.github.masamune.ai

import State
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Animation
import io.github.masamune.component.AnimationType
import io.github.masamune.component.Move

data object AnimationStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo = AnimationType.IDLE
    }

    override fun World.onUpdate(entity: Entity) {
        if (!entity[Move].direction.isZero) {
            entity[State].animationFsm.changeState(AnimationStateWalk)
        }
    }
}

data object AnimationStateWalk : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo = AnimationType.WALK
    }

    override fun World.onUpdate(entity: Entity) {
        if (entity[Move].direction.isZero) {
            entity[State].animationFsm.changeState(AnimationStateIdle)
        }
    }
}
