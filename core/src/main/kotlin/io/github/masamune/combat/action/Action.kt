package io.github.masamune.combat.action

import com.github.quillraven.fleks.Entity
import io.github.masamune.combat.ActionExecutorService

enum class ActionTargetType {
    NONE, SINGLE, MULTI, ALL
}

sealed class Action(val targetType: ActionTargetType) {
    open fun ActionExecutorService.canPerform(entity: Entity): Boolean = true
    open fun ActionExecutorService.onStart() = Unit
    open fun ActionExecutorService.onUpdate(): Boolean = true
    open fun ActionExecutorService.onFinish() = Unit
}

data object DefaultAction : Action(ActionTargetType.NONE)
