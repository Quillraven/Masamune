package io.github.masamune.system

import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntity
import com.github.quillraven.fleks.collection.mutableEntityBagOf
import io.github.masamune.component.Combat
import io.github.masamune.component.Stats
import ktx.log.logger

private enum class CombatState {
    CHOOSE_ACTION,
    PERFORM_ACTION,
}

class CombatSystem : IntervalSystem() {

    private val combatEntities = family { all(Combat, Stats) }
    private var state = CombatState.CHOOSE_ACTION
    private val actionEntities = mutableEntityBagOf()

    override fun onTick() {
        when (state) {
            CombatState.CHOOSE_ACTION -> {
                if (combatEntities.isNotEmpty && combatEntities.entities.all { it[Combat].hasAction }) {
                    // all entities made their decision -> start combat round
                    // sort entities by their agility -> higher agility goes first
                    combatEntities.sort(compareEntity(world) { e1, e2 ->
                        (e1[Stats].tiledStats.agility - e2[Stats].tiledStats.agility).toInt()
                    })
                    actionEntities.clear()
                    combatEntities.forEach { actionEntities += it }
                    log.debug {
                        buildString {
                            append("Starting combat round for:\n")
                            append(actionEntities.map { "Entity ${it.id} life ${it[Stats].tiledStats.life} -> ${it[Combat].action}" }
                                .joinToString("\n"))
                        }
                    }
                    state = CombatState.PERFORM_ACTION
                }
            }

            CombatState.PERFORM_ACTION -> {
                val actionEntity = actionEntities.first()
                val action = actionEntity[Combat].action
                log.debug { "$actionEntity performing $action" }
                actionEntities -= actionEntity
                actionEntity[Combat].action.run { world.onUpdate(deltaTime) }
                actionEntity[Combat].clearAction()
                if (actionEntities.isEmpty()) {
                    log.debug { "Combat round finished" }
                    state = CombatState.CHOOSE_ACTION
                }
            }
        }

    }

    companion object {
        private val log = logger<CombatSystem>()
    }
}
