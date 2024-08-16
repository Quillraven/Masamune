package io.github.masamune.ai

import State
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.bag
import io.github.masamune.component.*
import io.github.masamune.tiledmap.AnimationType

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

data object GlobalAnimationStateFacing : FsmState {

    private val previousEntityFacing = bag<FacingDirection>(100)

    override fun World.onUpdate(entity: Entity) {
        val currentFacing = entity[Facing].direction
        val previousFacing = previousEntityFacing.getOrNull(entity.id)
        if (previousFacing == null) {
            previousEntityFacing[entity.id] = currentFacing
            return
        } else if (previousFacing != currentFacing) {
            entity[Animation].changeFacingTo = currentFacing
            previousEntityFacing[entity.id] = currentFacing
        }
    }
}
