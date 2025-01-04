package io.github.masamune.combat.state

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.compareEntity
import io.github.masamune.combat.ActionExecutorService
import io.github.masamune.component.AI
import io.github.masamune.component.Combat
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.event.CombatTurnBeginEvent
import io.github.masamune.event.EventService
import io.github.masamune.isEntityDead
import ktx.log.logger

// state is entered, when player decided the actions for next round
// this state then calls enemy AI to pick their actions
class CombatStatePrepareRound(
    private val world: World,
    private val eventService: EventService = world.inject(),
    private val actionExecutorService: ActionExecutorService = world.inject(),
) : CombatState {
    private val combatEntities = world.family { all(Combat) }
    private val enemyEntities = world.family { none(Player).all(Combat) }

    // sort entities by their agility -> higher agility goes first
    private val comparator = compareEntity(world) { e1, e2 ->
        (e2[Stats].totalAgility - e1[Stats].totalAgility).toInt()
    }
    var turn = 0

    override fun onEnter() {
        if (turn == 0) {
            // execute passive actions with onCombatStart function
            actionExecutorService.performPassiveActions(combatEntities.entities) { action ->
                action.run { actionExecutorService.onCombatStart() }
            }
        }

        enemyEntities.forEach { enemy ->
            if (world.isEntityDead(enemy)) {
                return@forEach
            }

            enemy[AI].behaviorTree.step()
        }

        // sort entities by their agility
        combatEntities.sort(comparator)

        log.debug { debugRound() }
        eventService.fire(CombatTurnBeginEvent(turn++))
    }


    private fun debugRound() = buildString {
        append("Combat turn $turn with ${combatEntities.numEntities} entities")
        appendLine()
        combatEntities.forEach { entity ->
            append("Entity ${entity.id} (")
            append(if (entity has Player) "Player" else "Enemy")
            append("): ")
            val stats = entity[Stats]
            val action = entity[Combat].action
            val targets = entity[Combat].targets
            val buffs = entity[Combat].buffs.joinToString { "${it::class.simpleName}" }
            append("life=${stats.life}, mana=${stats.mana}, agi=${stats.agility}, action=$action, targets=$targets, buffs=$buffs")
            appendLine()
        }
    }

    companion object {
        private val log = logger<CombatStatePrepareRound>()
    }
}
