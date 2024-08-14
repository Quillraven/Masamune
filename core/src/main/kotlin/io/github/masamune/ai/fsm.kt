package io.github.masamune.ai

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

sealed interface FsmState : State<Entity> {
    fun World.onEnter(entity: Entity) = Unit

    override fun enter(entity: Entity) = Unit

    fun World.onUpdate(entity: Entity) = Unit

    override fun update(entity: Entity) = Unit

    fun World.onExit(entity: Entity) = Unit

    override fun exit(entity: Entity) = Unit

    override fun onMessage(entity: Entity, telegram: Telegram): Boolean = true
}

class FleksStateMachine(
    private val world: World,
    owner: Entity,
    initialState: FsmState,
    globalState: FsmState? = null,
) : DefaultStateMachine<Entity, FsmState>(owner, null, globalState) {

    init {
        // call changeState instead of passing the initialState as constructor
        // to correctly call 'enter' of the initial state.
        changeState(initialState)
    }

    override fun update() {
        // Execute the global state (if any)
        globalState?.run { world.onUpdate(owner) }

        // Execute the current state (if any)
        currentState?.run { world.onUpdate(owner) }
    }

    override fun changeState(newState: FsmState?) {
        // Keep a record of the previous state
        previousState = currentState

        // Call the exit method of the existing state
        currentState?.run { world.onExit(owner) }

        // Change state to the new state
        currentState = newState

        // Call the entry method of the new state
        currentState?.run { world.onEnter(owner) }
    }
}
