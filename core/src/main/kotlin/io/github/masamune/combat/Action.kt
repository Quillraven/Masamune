package io.github.masamune.combat

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.EntityBag
import io.github.masamune.audio.AudioService
import io.github.masamune.component.Stats

enum class ActionTargetType {
    NONE, SINGLE, MULTI, ALL
}

enum class ActionState {
    START, UPDATE, FINISH, END;
}

class Action(
    val world: World,
    val source: Entity,
    val effect: ActionEffect,
    val targets: EntityBag,
    val audioService: AudioService = world.inject(),
) {
    private var state: ActionState = ActionState.START

    val isFinished: Boolean
        get() = state == ActionState.END


    val singleTarget: Entity
        get() = targets.first()

    val deltaTime: Float
        get() = world.deltaTime

    private fun changeState(newState: ActionState) {
        if (newState.ordinal > state.ordinal) {
            state = newState
        }
    }

    fun update() {
        when (state) {
            ActionState.START -> {
                effect.run { this@Action.onStart() }
                changeState(ActionState.UPDATE)
            }

            ActionState.UPDATE -> {
                effect.run {
                    if (this@Action.onUpdate()) {
                        changeState(ActionState.FINISH)
                    }
                }

            }

            ActionState.FINISH -> {
                effect.run { this@Action.onFinish() }
                changeState(ActionState.END)
            }

            ActionState.END -> Unit
        }
    }

    fun attack(target: Entity) = with(world) {
        target[Stats].life -= source[Stats].damage
    }

    fun dealDamage(physical: Float, magical: Float, target: Entity) = with(world) {
        target[Stats].life -= (physical + magical)
    }

    fun endAction() {
        state = ActionState.END
    }

    fun hasMana(amount: Int): Boolean = with(world) {
        source[Stats].mana >= amount
    }

    override fun toString(): String {
        return "Action(effect=${effect::class.simpleName}, entity=${source.id}, state=$state)"
    }
}

sealed class ActionEffect(val targetType: ActionTargetType) {
    open fun Action.canPerform(entity: Entity): Boolean = true
    open fun Action.onStart() = Unit
    open fun Action.onUpdate(): Boolean = true
    open fun Action.onFinish() = Unit
}

data object DefaultActionEffect : ActionEffect(ActionTargetType.NONE)

class AttackActionEffect : ActionEffect(ActionTargetType.SINGLE) {
    override fun Action.onStart() {
        attack(singleTarget)
        endAction()
    }
}

class FireballActionEffect : ActionEffect(ActionTargetType.ALL) {
    private var timer = 0.5f;
    private var amount = 2;

    override fun Action.canPerform(entity: Entity) = hasMana(5)

    override fun Action.onUpdate(): Boolean {
        if (amount <= 0) {
            return true
        }

        timer -= deltaTime
        if (timer <= 0f) {
            timer = 0.5f
            amount--
            targets.forEach { dealDamage(physical = 0f, magical = 10f, it) }
        }
        return false
    }
}
