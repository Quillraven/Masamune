package io.github.masamune.combat.effect

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.combat.ActionState
import io.github.masamune.combat.effect.DefaultEffect.target
import io.github.masamune.event.CombatEntityDeadEvent
import io.github.masamune.event.CombatEntityHealEvent
import io.github.masamune.event.CombatEntityManaUpdateEvent
import io.github.masamune.event.CombatEntityTakeDamageEvent
import io.github.masamune.event.EventService
import io.github.masamune.isEntityDead

sealed interface Effect {
    val source: Entity
    val target: Entity

    fun World.onStart() = Unit
    fun World.onUpdate(): Boolean = true
}

data object DefaultEffect : Effect {
    override val source: Entity = Entity.NONE
    override val target: Entity = Entity.NONE
}

data class EffectStack(
    private val world: World,
    private val stack: ArrayDeque<Effect> = ArrayDeque(),
    private val eventService: EventService = world.inject(),
) {
    private var currentEffect: Effect = DefaultEffect

    val isNotEmpty: Boolean
        get() = stack.isNotEmpty() || currentEffect != DefaultEffect

    val last: Effect
        get() = stack.last()

    fun clear() {
        stack.clear()
        currentEffect = DefaultEffect
    }

    fun startNext() {
        if (stack.isEmpty()) {
            currentEffect = DefaultEffect
            return
        }

        currentEffect = stack.removeFirst()
        currentEffect.run { world.onStart() }
    }

    fun addLast(effect: Effect) = stack.addLast(effect)

    fun addAfter(effect: Effect, add: DelayEffect) {
        val idx = stack.indexOf(effect)
        stack.add(idx + 1, add)
    }

    fun update(): Boolean {
        if (stack.isEmpty() && currentEffect === DefaultEffect) {
            // no effects to perform
            return true
        }

        val effect = currentEffect
        // effect currently in progress
        if (effect.run { world.onUpdate() }) {
            // effect finished

            // 1) fire special events if necessary
            if (effect is DamageEffect && effect.damage > 0f) {
                eventService.fire(
                    CombatEntityTakeDamageEvent(
                        effect.target,
                        effect.damage,
                        effect.targetLife,
                        effect.targetLifeMax,
                        effect.critical
                    )
                )

                if (world.isEntityDead(effect.target)) {
                    // entity is dead -> remove any of its effects from the stack
                    stack.removeIf { it.target == effect.target || it.source == effect.target }
                    eventService.fire(CombatEntityDeadEvent(effect.target))
                }
            } else if (effect is HealEffect) {
                if (effect.amountLife > 0f) {
                    eventService.fire(
                        CombatEntityHealEvent(
                            target,
                            effect.amountLife,
                            effect.targetLife,
                            effect.targetLifeMax
                        )
                    )
                }
                if (effect.amountMana > 0f) {
                    eventService.fire(
                        CombatEntityManaUpdateEvent(
                            target,
                            effect.amountMana,
                            effect.targetMana,
                            effect.targetManaMax,
                            ActionState.FINISH
                        )
                    )
                }
            }

            // 2) get next effect
            startNext()
        }

        return currentEffect === DefaultEffect
    }
}
