package io.github.masamune.combat

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.EntityBag
import io.github.masamune.asset.SoundAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.effect.DefaultEffect
import io.github.masamune.combat.effect.Effect
import io.github.masamune.component.Combat
import io.github.masamune.component.Stats
import kotlin.math.max

enum class ActionTargetType {
    NONE, SINGLE, MULTI, ALL
}

enum class ActionState {
    START, UPDATE, FINISH, END;
}

class Action(
    private val world: World,
    private val source: Entity,
    private var effect: Effect,
    private val targets: EntityBag,
    private val audioService: AudioService = world.inject(),
) {
    private var state: ActionState = ActionState.START

    private var delaySec = 0f

    val isFinished: Boolean
        get() = state == ActionState.END && delaySec <= 0f


    val singleTarget: Entity
        get() = targets.first()

    val allTargets: EntityBag
        get() = targets

    val numTargets: Int
        get() = targets.size

    val deltaTime: Float
        get() = world.deltaTime

    private fun changeState(newState: ActionState) {
        if (newState.ordinal > state.ordinal) {
            state = newState
        }
    }

    fun update() {
        if (delaySec > 0f) {
            delaySec = max(0f, delaySec - deltaTime)
            return
        }

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

    /**
     * Performs an attack against the [target] entity and waits [delay] seconds before continuing.
     */
    fun attack(target: Entity, delay: Float = 1f) = with(world) {
        target[Stats].life -= source[Stats].damage
        play(source[Combat].attackSnd, delay)
    }

    fun wait(seconds: Float) {
        delaySec += seconds
    }

    fun dealDamage(physical: Float, magical: Float, target: Entity) = with(world) {
        target[Stats].life -= (physical + magical)
    }

    fun dealDamage(physical: Float, magical: Float, targets: EntityBag) = with(world) {
        targets.forEach { dealDamage(physical, magical, it) }
    }

    fun endAction() {
        state = ActionState.END
    }

    fun hasMana(amount: Int): Boolean = with(world) {
        source[Stats].mana >= amount
    }

    fun play(soundAsset: SoundAsset, delay: Float = 0.5f) {
        audioService.play(soundAsset, pitch = MathUtils.random(0.7f, 1.3f))
        wait(delay)
    }

    fun clearEffect() = with(world) {
        source[Combat].clearEffect()
        effect = DefaultEffect
    }

    override fun toString(): String {
        return "Action(effect=${effect::class.simpleName}, entity=${source.id}, state=$state, delay=$delaySec)"
    }
}


