package io.github.masamune.combat.state

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.compareEntity
import io.github.masamune.combat.ActionExecutorService
import io.github.masamune.combat.action.AttackSingleAction
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
    private val playerEntities = world.family { all(Player, Combat) }

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

        // TODO pick enemy action based on their AI
        enemyEntities.forEach { enemy ->
            if (world.isEntityDead(enemy)) {
                return@forEach
            }

            enemy[Combat].run {
                action = AttackSingleAction()
                targets.clear()
                targets += playerEntities.first()
            }
        }

        // sort entities by their agility
        combatEntities.sort(comparator)

        log.debug { "Combat turn $turn with ${combatEntities.numEntities} entities" }
        eventService.fire(CombatTurnBeginEvent(turn++))
    }

    companion object {
        private val log = logger<CombatStatePrepareRound>()
    }
}
