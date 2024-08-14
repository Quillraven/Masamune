package io.github.masamune.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

sealed interface FsmState {
    fun World.onEnter(entity: Entity) = Unit

    fun World.onUpdate(entity: Entity) = Unit

    fun World.onExit(entity: Entity) = Unit
}

class FleksStateMachine(
    private val world: World,
    private val entity: Entity,
    private var currentState: FsmState,
    private var globalState: FsmState? = null,
) {
    private var previousState: FsmState? = null

    init {
        changeState(currentState)
    }

    fun update() {
        // Execute the global state (if any)
        globalState?.run { world.onUpdate(entity) }

        // Execute the current state (if any)
        currentState.run { world.onUpdate(entity) }
    }

    fun changeState(newState: FsmState) {
        // Keep a record of the previous state
        previousState = currentState

        // Call the exit method of the existing state
        currentState.run { world.onExit(entity) }

        // Change state to the new state
        currentState = newState

        // Call the entry method of the new state
        currentState.run { world.onEnter(entity) }
    }
}
