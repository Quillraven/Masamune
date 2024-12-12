package io.github.masamune.system

import com.github.quillraven.fleks.IntervalSystem
import io.github.masamune.combat.CombatState
import io.github.masamune.combat.CombatStateCheckVictoryDefeat
import io.github.masamune.combat.CombatStateDefeat
import io.github.masamune.combat.CombatStateIdle
import io.github.masamune.combat.CombatStatePerformAction
import io.github.masamune.combat.CombatStatePrepareRound
import io.github.masamune.combat.CombatStateVictory
import io.github.masamune.event.CombatNextTurnEvent
import io.github.masamune.event.CombatPlayerActionEvent
import io.github.masamune.event.CombatPlayerDefeatEvent
import io.github.masamune.event.CombatPlayerVictoryEvent
import io.github.masamune.event.CombatStartEvent
import io.github.masamune.event.CombatTurnBeginEvent
import io.github.masamune.event.Event
import io.github.masamune.event.EventListener
import ktx.log.logger

class CombatSystem : IntervalSystem(), EventListener {
    private val states = listOf(
        CombatStateIdle,
        CombatStatePrepareRound(world),
        CombatStatePerformAction(world),
        CombatStateVictory(world),
        CombatStateDefeat(world),
    )
    private var prevState: CombatState = CombatStateIdle
    private var currentState: CombatState = CombatStateIdle
    private var globalState: CombatState = CombatStateIdle

    override fun onTick() {
        globalState.onUpdate(deltaTime)
        currentState.onUpdate(deltaTime)
    }

    private inline fun <reified S : CombatState> changeState() {
        val nextState = states.filterIsInstance<S>().single()
        if (currentState == nextState) {
            return
        }

        log.debug { "Switching combat state from $prevState to $nextState" }
        prevState = currentState
        prevState.onExit()
        currentState = nextState
        currentState.onEnter()
    }

    override fun onEvent(event: Event) {
        when (event) {
            is CombatStartEvent -> {
                globalState = CombatStateCheckVictoryDefeat(world)
                changeState<CombatStateIdle>()
            }

            is CombatNextTurnEvent -> changeState<CombatStateIdle>()
            is CombatPlayerActionEvent -> changeState<CombatStatePrepareRound>()
            is CombatTurnBeginEvent -> changeState<CombatStatePerformAction>()
            is CombatPlayerDefeatEvent -> {
                globalState = CombatStateIdle
                changeState<CombatStateDefeat>()
            }

            is CombatPlayerVictoryEvent -> {
                globalState = CombatStateIdle
                changeState<CombatStateVictory>()
            }

            else -> Unit
        }
    }

    companion object {
        private val log = logger<CombatSystem>()
    }
}
