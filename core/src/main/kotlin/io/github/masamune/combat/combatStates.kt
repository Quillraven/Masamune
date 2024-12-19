package io.github.masamune.combat

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.compareEntity
import io.github.masamune.asset.MusicAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.component.Animation
import io.github.masamune.component.Combat
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.component.isEntityDead
import io.github.masamune.event.CombatNextTurnEvent
import io.github.masamune.event.CombatPlayerDefeatEvent
import io.github.masamune.event.CombatPlayerVictoryEvent
import io.github.masamune.event.CombatTurnBeginEvent
import io.github.masamune.event.CombatTurnEndEvent
import io.github.masamune.event.EventService
import io.github.masamune.tiledmap.AnimationType
import ktx.log.Logger

private val log = Logger("CombatState")

sealed interface CombatState {
    fun onEnter() = Unit

    fun onUpdate(deltaTime: Float) = Unit

    fun onExit() = Unit
}

data object CombatStateIdle : CombatState

// state is entered, when player decided the actions for next round
// this state then calls enemy AI to pick their actions
class CombatStatePrepareRound(
    private val world: World,
    private val eventService: EventService = world.inject(),
) : CombatState {
    private val combatEntities = world.family { all(Combat) }
    private val enemyEntities = world.family { none(Player).all(Combat) }
    private val playerEntities = world.family { all(Player, Combat) }

    // sort entities by their agility -> higher agility goes first
    private val comparator = compareEntity(world) { e1, e2 ->
        (e2[Stats].agility - e1[Stats].agility).toInt()
    }
    private var turn = 0

    override fun onEnter() {
        // TODO pick enemy action based on their AI
        enemyEntities.forEach { enemy ->
            enemy[Combat].run {
                effect = AttackActionEffect()
                targets.clear()
                targets += playerEntities.first()
            }
        }

        // sort entities by their agility
        combatEntities.sort(comparator)

        log.debug { "Combat turn $turn with ${combatEntities.numEntities} entities" }
        eventService.fire(CombatTurnBeginEvent(turn++))
    }
}

class CombatStatePerformAction(
    private val world: World,
    private val eventService: EventService = world.inject(),
) : CombatState {
    private val combatEntities = world.family { all(Combat) }
    private val actionStack = ArrayDeque<Action>()
    private var turnEnd = false

    override fun onEnter() {
        log.debug { debugCombatEntities() }
        turnEnd = false
        actionStack.clear()
        combatEntities.forEach { entity ->
            val (effect, targets) = entity[Combat]
            actionStack += Action(world, entity, effect, targets)
        }
    }

    private fun debugCombatEntities() = buildString {
        append("Combat Stats debug:")
        appendLine()
        combatEntities.forEach { entity ->
            append("Entity ${entity.id} (")
            append(if (entity has Player) "Player" else "Enemy")
            append(") ")
            append("Life: ${entity[Stats].life}")
            appendLine()
        }
    }

    override fun onUpdate(deltaTime: Float) {
        if (actionStack.isEmpty()) {
            if (!turnEnd) {
                // all actions of turn performed -> fire turn end event which might add some actions on the stack again
                log.debug { "Combat turn end" }
                turnEnd = true
                eventService.fire(CombatTurnEndEvent)
            } else {
                log.debug { "Combat trigger next turn" }
                eventService.fire(CombatNextTurnEvent)
            }
            return
        }

        val action = actionStack.first()
        log.debug { "${action.source} performing $action" }
        action.update()
        if (action.isFinished) {
            actionStack.removeFirst()
            with(world) {
                action.source[Combat].clearEffect()
            }
        }
    }

    override fun onExit() {
        log.debug { debugCombatEntities() }
    }
}

class CombatStateVictory(
    private val world: World,
    private val audioService: AudioService = world.inject(),
) : CombatState {
    override fun onEnter() {
        audioService.play(MusicAsset.COMBAT_VICTORY, loop = false, keepPrevious = true)
        log.debug { "Combat victory!" }
    }
}

class CombatStateDefeat(
    private val world: World,
    private val audioService: AudioService = world.inject(),
) : CombatState {
    private val playerEntities = world.family { all(Player, Combat) }

    override fun onEnter() {
        log.debug { "Combat defeat!" }
        audioService.play(MusicAsset.COMBAT_DEFEAT, loop = false, keepPrevious = true)
        playerEntities.forEach { entity ->
            entity[Animation].changeTo = AnimationType.DEAD
        }
    }
}

class CombatStateCheckVictoryDefeat(
    private val world: World,
    private val eventService: EventService = world.inject(),
) : CombatState {
    private val enemyEntities = world.family { none(Player).all(Combat) }
    private val playerEntities = world.family { all(Player, Combat) }

    override fun onUpdate(deltaTime: Float) {
        if (playerEntities.all { world.isEntityDead(it) }) {
            log.debug { "All player entities dead -> Defeat" }
            eventService.fire(CombatPlayerDefeatEvent)
        } else if (enemyEntities.all { world.isEntityDead(it) }) {
            log.debug { "All enemy entities dead -> Victory" }
            eventService.fire(CombatPlayerVictoryEvent)
        }
    }
}
