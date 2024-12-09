package io.github.masamune.combat

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.mutableEntityBagOf
import io.github.masamune.component.Stats

enum class ActionTargetType {
    NONE, SINGLE, MULTI, ALL
}

sealed class Action(val entity: Entity, val targetType: ActionTargetType) {
    val targets = mutableEntityBagOf()

    val singleTarget: Entity
        get() = targets.first()

    fun World.isPossible(): Boolean = true

    fun World.onStart() = Unit

    abstract fun World.onUpdate(deltaTime: Float): Boolean

    fun World.onFinish() = Unit
}

data object DefaultAction : Action(Entity.NONE, ActionTargetType.NONE) {
    override fun World.onUpdate(deltaTime: Float): Boolean = true
}

class AttackAction(entity: Entity) : Action(entity, ActionTargetType.SINGLE) {

    override fun World.onUpdate(deltaTime: Float): Boolean {
        singleTarget[Stats].tiledStats.life -= entity[Stats].tiledStats.damage
        return true
    }

}
