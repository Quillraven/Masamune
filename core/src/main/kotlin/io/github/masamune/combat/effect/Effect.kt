package io.github.masamune.combat.effect

import com.github.quillraven.fleks.Entity
import io.github.masamune.combat.Action
import io.github.masamune.combat.ActionTargetType

sealed class Effect(val targetType: ActionTargetType) {
    open fun Action.canPerform(entity: Entity): Boolean = true
    open fun Action.onStart() = Unit
    open fun Action.onUpdate(): Boolean = true
    open fun Action.onFinish() = Unit
}

data object DefaultEffect : Effect(ActionTargetType.NONE)
