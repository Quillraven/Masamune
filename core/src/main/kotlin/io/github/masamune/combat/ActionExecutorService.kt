package io.github.masamune.combat

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.EntityBag
import com.github.quillraven.fleks.collection.mutableEntityBagOf
import io.github.masamune.asset.SoundAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.action.Action
import io.github.masamune.combat.action.DefaultAction
import io.github.masamune.component.Combat
import io.github.masamune.component.Inventory.Companion.removeItem
import io.github.masamune.component.Item
import io.github.masamune.component.Stats
import io.github.masamune.event.CombatEntityDeadEvent
import io.github.masamune.event.CombatEntityHealEvent
import io.github.masamune.event.CombatEntityManaUpdateEvent
import io.github.masamune.event.CombatEntityTakeDamageEvent
import io.github.masamune.event.EventService
import ktx.log.logger
import kotlin.math.abs
import kotlin.math.max

private enum class ActionState {
    START, UPDATE, FINISH, END;
}

class ActionExecutorService(
    private val audioService: AudioService,
    private val eventService: EventService,
) {
    private var state: ActionState = ActionState.START
    private var delaySec = 0f
    private val targets = mutableEntityBagOf()
    var source: Entity = Entity.NONE
        private set
    private var action: Action = DefaultAction
    private var itemOwner: Entity = Entity.NONE
    lateinit var world: World

    val isFinished: Boolean
        get() = action == DefaultAction || (state == ActionState.END && delaySec <= 0f)

    val singleTarget: Entity
        get() = targets.first()

    val allTargets: EntityBag
        get() = targets

    val numTargets: Int
        get() = targets.size

    val deltaTime: Float
        get() = world.deltaTime

    val Entity.stats: Stats
        get() = with(world) { this@stats[Stats] }

    val Entity.itemAction: Action
        get() = with(world) { this@itemAction[Item].action }

    fun perform(source: Entity, action: Action, targets: EntityBag) {
        log.debug { "Performing action ${action::class.simpleName}: source=$source, targets(${targets.size})=$targets" }

        this.state = ActionState.START
        this.delaySec = 0f
        this.source = source
        this.action = action
        this.targets.clear()
        this.targets += targets
    }

    fun performItemAction(itemOwner: Entity, item: Entity, action: Action, targets: EntityBag) {
        this.itemOwner = itemOwner
        with(world) { removeItem(item[Item].type, 1, itemOwner) }
        perform(item, action, targets)
    }

    private fun changeState(newState: ActionState) {
        // never go back to a previous state (can happen if 'end' is called during an update).
        // In that case we want to remain in END state
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
                updateManaBy(source, -action.manaCost.toFloat())
                action.run { this@ActionExecutorService.onStart() }
                changeState(ActionState.UPDATE)
            }

            ActionState.UPDATE -> {
                action.run {
                    if (this@ActionExecutorService.onUpdate()) {
                        changeState(ActionState.FINISH)
                    }
                }

            }

            ActionState.FINISH -> {
                action.run { this@ActionExecutorService.onFinish() }
                changeState(ActionState.END)
            }

            ActionState.END -> Unit
        }
    }

    /**
     * Performs an attack against the [target] entity and waits [delay] seconds before continuing.
     */
    fun attack(target: Entity, delay: Float = 1f) = with(world) {
        val sourceStats = source[Stats]
        updateLifeBy(target, -(sourceStats.strength + sourceStats.damage))
        play(source[Combat].attackSnd, delay)
    }

    fun wait(seconds: Float) {
        delaySec += seconds
    }

    private fun updateManaBy(target: Entity, amount: Float) = with(world) {
        val targetStats = target[Stats]
        targetStats.mana = (targetStats.mana + amount).coerceIn(0f, targetStats.manaMax)
        if (amount != 0f) {
            eventService.fire(CombatEntityManaUpdateEvent(target, abs(amount), targetStats.mana, targetStats.manaMax))
        }
    }

    private fun updateLifeBy(target: Entity, amount: Float) = with(world) {
        val targetStats = target[Stats]
        targetStats.life = (targetStats.life + amount).coerceIn(0f, targetStats.lifeMax)

        if (amount < 0f) {
            eventService.fire(CombatEntityTakeDamageEvent(target, -amount, targetStats.life, targetStats.lifeMax))
        } else if (amount > 0f) {
            eventService.fire(CombatEntityHealEvent(target, amount, targetStats.life, targetStats.lifeMax))
        }

        if (targetStats.life <= 0f) {
            log.debug { "$target is dead" }
            eventService.fire(CombatEntityDeadEvent(target))
        }
    }

    fun dealDamage(physical: Float, magical: Float, target: Entity) {
        updateLifeBy(target, -(physical + magical))
    }

    fun dealDamage(physical: Float, magical: Float, targets: EntityBag) {
        targets.forEach { dealDamage(physical, magical, it) }
    }

    fun heal(life: Float, mana: Float, target: Entity) {
        if (life > 0f) {
            updateLifeBy(target, life)
        }
        if (mana > 0f) {
            updateManaBy(target, mana)
        }
    }

    fun endAction() {
        state = ActionState.END
    }

    fun hasMana(entity: Entity, amount: Int): Boolean = with(world) {
        entity[Stats].mana >= amount
    }

    fun play(soundAsset: SoundAsset, delay: Float = 0.5f) {
        audioService.play(soundAsset, pitch = MathUtils.random(0.7f, 1.3f))
        wait(delay)
    }

    fun clearAction() = with(world) {
        if (itemOwner != Entity.NONE) {
            itemOwner[Combat].clearAction()
            itemOwner = Entity.NONE
        } else {
            source[Combat].clearAction()
        }
        action = DefaultAction
    }

    override fun toString(): String {
        return "Action(effect=${action::class.simpleName}, entity=${source.id}, state=$state, delay=$delaySec)"
    }

    companion object {
        private val log = logger<ActionExecutorService>()
    }
}


