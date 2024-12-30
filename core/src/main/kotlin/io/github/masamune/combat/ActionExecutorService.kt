package io.github.masamune.combat

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.EntityBag
import com.github.quillraven.fleks.collection.MutableEntityBag
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.CachingAtlas
import io.github.masamune.asset.SoundAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.ActionQueueEntry.Companion.DEFAULT_QUEUE_ACTION
import io.github.masamune.combat.action.Action
import io.github.masamune.combat.action.DefaultAction
import io.github.masamune.combat.buff.Buff
import io.github.masamune.combat.buff.BuffOnAttackDamage
import io.github.masamune.component.Animation
import io.github.masamune.component.Combat
import io.github.masamune.component.Graphic
import io.github.masamune.component.Item
import io.github.masamune.component.MoveBy
import io.github.masamune.component.Player
import io.github.masamune.component.Remove
import io.github.masamune.component.Stats
import io.github.masamune.component.Transform
import io.github.masamune.event.CombatEntityDeadEvent
import io.github.masamune.event.CombatEntityHealEvent
import io.github.masamune.event.CombatEntityManaUpdateEvent
import io.github.masamune.event.CombatEntityTakeDamageEvent
import io.github.masamune.event.CombatMissEvent
import io.github.masamune.event.CombatNextTurnEvent
import io.github.masamune.event.CombatTurnEndEvent
import io.github.masamune.event.EventService
import io.github.masamune.isEntityAlive
import io.github.masamune.isEntityDead
import io.github.masamune.tiledmap.AnimationType
import ktx.log.logger
import ktx.math.vec2
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

enum class ActionState {
    START, UPDATE, FINISH, END;
}

data class ActionQueueEntry(val entity: Entity, val action: Action, val targets: EntityBag) {
    companion object {
        val DEFAULT_QUEUE_ACTION = ActionQueueEntry(Entity.NONE, DefaultAction, MutableEntityBag(0))
    }
}


class ActionExecutorService(
    val audioService: AudioService,
    val eventService: EventService,
) {
    private val actionStack = ArrayDeque<ActionQueueEntry>()
    private var state: ActionState = ActionState.START
    private var delaySec = 0f
    var currentQueueEntry: ActionQueueEntry = DEFAULT_QUEUE_ACTION
        private set
    private var itemOwner: Entity = Entity.NONE
    private var endTurnPerformed = false

    lateinit var world: World
        private set
    private lateinit var sfxAtlas: CachingAtlas
    private lateinit var allEnemies: Family
    private lateinit var allPlayers: Family

    private val isFinished: Boolean
        get() = currentQueueEntry.action == DefaultAction || (state == ActionState.END && delaySec <= 0f)

    inline val source: Entity
        get() = currentQueueEntry.entity

    inline val action: Action
        get() = currentQueueEntry.action

    inline val singleTarget: Entity
        get() = currentQueueEntry.targets.first()

    inline val allTargets: EntityBag
        get() = currentQueueEntry.targets

    inline val numTargets: Int
        get() = currentQueueEntry.targets.size

    inline val deltaTime: Float
        get() = world.deltaTime

    inline val Entity.stats: Stats
        get() = with(world) { this@stats[Stats] }

    inline val Entity.itemAction: Action
        get() = with(world) { this@itemAction[Item].action }

    infix fun withWorld(world: World) {
        this.world = world
        this.sfxAtlas = world.inject(AtlasAsset.SFX.name)
        this.allEnemies = world.family { none(Player).all(Combat) }
        this.allPlayers = world.family { all(Player, Combat) }
    }

    fun queueAction(entity: Entity, action: Action, targets: EntityBag) {
        actionStack += ActionQueueEntry(entity, action, targets)
    }

    private fun perform(queueEntry: ActionQueueEntry, moveEntity: Boolean = true) {
        val (source, action, targets) = queueEntry
        log.debug { "Performing action ${action::class.simpleName}: source=$source, targets(${targets.size})=$targets" }

        this.state = ActionState.START
        this.delaySec = 0f
        this.currentQueueEntry = queueEntry
        if (moveEntity) {
            moveEntityBy(queueEntry.entity, PERFORM_OFFSET, 0.5f)
        }
    }

    fun performItemAction(itemOwner: Entity, item: Entity, action: Action, targets: EntityBag) {
        this.itemOwner = itemOwner
        with(world) {
            // Just reduce amount instead of calling world.removeItem because
            // removeItem will remove the item entity already, but we still need it to get its stats, etc..
            // The real item removal happens at the end of the combat in the CombatScreen.
            --item[Item].amount
        }
        perform(ActionQueueEntry(item, action, targets), false)
    }

    private fun moveEntityBy(entity: Entity, amount: Float, duration: Float) = with(world) {
        wait(duration + 0.25f)
        entity.configure {
            val direction = if (entity has Player) 1 else -1
            it += MoveBy(vec2(0f, amount * direction), duration, Interpolation.fastSlow)
        }
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

        if (actionStack.isEmpty()) {
            if (!endTurnPerformed) {
                // all actions of turn performed -> fire turn end event which might add some actions on the stack again
                log.debug { "Combat turn end" }
                endTurnPerformed = true
                eventService.fire(CombatTurnEndEvent)
            } else {
                log.debug { "Combat trigger next turn" }
                with(world) {
                    val player = allPlayers.single { it has Player }
                    val aliveEnemies = allEnemies.filter { it hasNo Player && isEntityAlive(it) }
                    eventService.fire(CombatNextTurnEvent(player, aliveEnemies))
                }
            }
            return
        }

        when (state) {
            ActionState.START -> {
                if (itemOwner == Entity.NONE) {
                    // action is not a use item action -> reduce mana cost of source entity
                    updateManaBy(source, -action.manaCost.toFloat())
                }
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

        if (isFinished) {
            actionStack.removeFirst()
            clearAction()
            performNext()
        }
    }

    /**
     * Performs an attack against the [target] entity and waits [delay] seconds before continuing.
     */
    fun attack(target: Entity, delay: Float = 1f) = with(world) {
        val realTarget = verifyTarget(target)
        if (realTarget == Entity.NONE) {
            // target is already dead and no other target is available -> do nothing
            return
        }

        // will target evade?
        val targetStats = realTarget[Stats]
        val evadeChance = targetStats.totalPhysicalEvade
        if (evadeChance > 0f && MathUtils.random() <= evadeChance) {
            eventService.fire(CombatMissEvent(realTarget))
            play(SoundAsset.ATTACK_MISS, delay)
            return@with
        }

        // add strength to physical damage
        val sourceStats = source[Stats]
        var damage = (sourceStats.totalStrength * DAM_PER_STR) + sourceStats.totalDamage

        // critical strike?
        val critChance = sourceStats.totalCriticalStrike
        val isCritical = critChance > 0f && MathUtils.random() <= critChance
        if (isCritical) {
            damage *= 2f
        }

        // reduce damage by armor
        val armor = targetStats.totalArmor
        val reduction = 100f / (100f + armor)
        damage *= reduction

        // apply damage
        val minDamage = ceil(damage * 0.9f)
        val maxDamage = floor(damage * 1.1f)
        damage = MathUtils.random(minDamage, maxDamage)
        source.applyBuffs<BuffOnAttackDamage> { damage = preAttackDamage(source, target, damage) }
        target.applyBuffs<BuffOnAttackDamage> { damage = preAttackDamage(source, target, damage) }
        updateLifeBy(realTarget, -damage, isCritical)
        target.applyBuffs<BuffOnAttackDamage> { damage = preAttackDamage(source, target, damage) }
        source.applyBuffs<BuffOnAttackDamage> { damage = preAttackDamage(source, target, damage) }

        // play sound and add SFX
        val combat = source[Combat]
        play(combat.attackSnd, delay)
        addSfx(realTarget, combat.attackSFX, duration = delay * 0.5f, scale = 2f)
    }

    private inline fun <reified T : Buff> Entity.applyBuffs(block: T.() -> Unit) = with(world) {
        this@applyBuffs[Combat].buffs.filterIsInstance<T>().forEach(block)
    }

    fun addSfx(to: Entity, sfxAtlasKey: String, duration: Float, scale: Float = 1f) = with(world) {
        val (toPos, toSize, toScale) = to[Transform]
        world.entity {
            it += Transform(toPos.cpy().apply { z = 3f }, toSize.cpy(), toScale * scale)
            val animation = Animation.ofAtlas(sfxAtlas, sfxAtlasKey, AnimationType.IDLE)
            animation.speed = 1f / (duration / animation.gdxAnimation.animationDuration)
            animation.playMode = PlayMode.NORMAL
            it += animation
            it += Graphic(animation.gdxAnimation.getKeyFrame(0f))
            it += Remove(duration)
        }
    }

    private fun wait(seconds: Float) {
        delaySec += seconds
    }

    private fun updateManaBy(target: Entity, amount: Float) = with(world) {
        val targetStats = target[Stats]
        targetStats.mana = (targetStats.mana + amount).coerceIn(0f, targetStats.totalManaMax)
        if (amount != 0f) {
            eventService.fire(
                CombatEntityManaUpdateEvent(
                    target,
                    abs(amount),
                    targetStats.mana,
                    targetStats.totalManaMax,
                    state
                )
            )
        }
    }

    private fun updateLifeBy(target: Entity, amount: Float, critical: Boolean) = with(world) {
        val targetStats = target[Stats]
        targetStats.life = (targetStats.life + amount).coerceIn(0f, targetStats.totalLifeMax)

        if (amount < 0f) {
            eventService.fire(
                CombatEntityTakeDamageEvent(
                    target,
                    -amount,
                    targetStats.life,
                    targetStats.totalLifeMax,
                    critical
                )
            )
        } else if (amount > 0f) {
            eventService.fire(CombatEntityHealEvent(target, amount, targetStats.life, targetStats.totalLifeMax))
        }

        if (targetStats.life <= 0f) {
            log.debug { "$target is dead" }
            eventService.fire(CombatEntityDeadEvent(target))
        }
    }

    private fun verifyTarget(target: Entity): Entity = with(world) {
        if (isEntityDead(target)) {
            // target is dead -> replace with a different target
            if (target hasNo Player) {
                return@with allEnemies.firstOrNull { it !in allTargets && isEntityAlive(it) } ?: Entity.NONE
            }
            return@with allPlayers.firstOrNull { it !in allTargets && isEntityAlive(it) } ?: Entity.NONE
        }
        return target
    }

    fun dealMagicDamage(
        amount: Float,
        target: Entity,
        sfxAtlasKey: String,
        sfxDuration: Float,
        sfxScale: Float,
        soundAsset: SoundAsset,
        delay: Float,
    ) = with(world) {
        val realTarget = verifyTarget(target)
        if (realTarget == Entity.NONE) {
            // target is already dead and no other target is available -> do nothing
            return@with
        }

        // will target evade?
        val targetStats = realTarget.stats
        val evadeChance = targetStats.totalMagicalEvade
        if (evadeChance > 0f && MathUtils.random() <= evadeChance) {
            eventService.fire(CombatMissEvent(realTarget))
            return@with
        }

        // add intelligence to magic damage
        val sourceStats = source[Stats]
        var damage = amount + (sourceStats.totalIntelligence * DAM_PER_INT)

        // arcane strike?
        val critChance = sourceStats.totalArcaneStrike
        val isCritical = critChance > 0f && MathUtils.random() <= critChance
        if (isCritical) {
            damage *= 2f
        }

        // reduce damage by resistance
        val resistance = targetStats.totalResistance
        val reduction = 100f / (100f + resistance)
        damage *= reduction

        // apply damage
        val minDamage = ceil(damage * 0.9f)
        val maxDamage = floor(damage * 1.1f)
        updateLifeBy(realTarget, -(MathUtils.random(minDamage, maxDamage)), isCritical)

        // add SFX
        addSfx(realTarget, sfxAtlasKey, sfxDuration, sfxScale)

        // add sound
        play(soundAsset, delay)
    }

    fun dealMagicDamage(
        amount: Float,
        targets: EntityBag,
        sfxAtlasKey: String,
        sfxDuration: Float,
        sfxScale: Float,
        soundAsset: SoundAsset,
        delay: Float,
    ) {
        targets.forEach { dealMagicDamage(amount, it, sfxAtlasKey, sfxDuration, sfxScale, soundAsset, 0f) }
        wait(delay)
    }

    fun heal(
        life: Float,
        mana: Float,
        target: Entity,
        sfxAtlasKey: String,
        sfxDuration: Float,
        sfxScale: Float,
        soundAsset: SoundAsset,
        delay: Float,
    ) {
        val realTarget = verifyTarget(target)
        if (realTarget == Entity.NONE) {
            // target is already dead and no other target is available -> do nothing
            return
        }

        if (life > 0f) {
            updateLifeBy(realTarget, life, false)
        }
        if (mana > 0f) {
            updateManaBy(realTarget, mana)
        }

        // add SFX
        addSfx(realTarget, sfxAtlasKey, sfxDuration, sfxScale)

        // add sound
        play(soundAsset, delay)
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
        log.debug { "Cleaning up ${this@ActionExecutorService}" }

        if (itemOwner != Entity.NONE) {
            moveEntityBy(itemOwner, -PERFORM_OFFSET, 0.3f)
            itemOwner[Combat].clearAction()
            itemOwner = Entity.NONE
        } else {
            moveEntityBy(source, -PERFORM_OFFSET, 0.3f)
            source[Combat].clearAction()
        }
        currentQueueEntry = DEFAULT_QUEUE_ACTION
    }

    inline fun <reified T : Buff> addBuff(buff: T) = with(world) {
        val ownerBuffs = buff.owner[Combat].buffs
        ownerBuffs.removeIf { it is T }
        ownerBuffs += buff
    }

    fun Buff.removeBuff() = with(world) {
        owner[Combat].buffs -= this@removeBuff
    }

    fun performFirst() {
        log.debug { "Starting first action of stack of size ${actionStack.size}" }
        perform(actionStack.first())
        endTurnPerformed = false
    }

    private fun performNext() {
        if (actionStack.isEmpty()) {
            return
        }

        var nextQueueEntry = actionStack.first()
        while (world.isEntityDead(nextQueueEntry.entity)) {
            // entity died already -> remove its action from the stack and find
            // next executable action
            actionStack.removeFirst()
            if (actionStack.isEmpty()) {
                return
            }
            nextQueueEntry = actionStack.first()
        }
        perform(nextQueueEntry)
    }

    override fun toString(): String {
        return "ActionExecutorService(action=${action::class.simpleName}, source=${source.id}, state=$state, delay=$delaySec)"
    }

    companion object {
        private val log = logger<ActionExecutorService>()
        private const val PERFORM_OFFSET = 0.75f // how many units will a unit move up/down when performing its action
        private const val DAM_PER_STR = 1 / 2f
        private const val DAM_PER_INT = 1 / 4f
    }
}


