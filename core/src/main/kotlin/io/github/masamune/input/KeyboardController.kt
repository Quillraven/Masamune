package io.github.masamune.input

import com.badlogic.gdx.Input.Keys
import io.github.masamune.event.*
import ktx.app.KtxInputAdapter
import ktx.app.gdxError
import kotlin.reflect.KClass

class KeyboardController(
    eventService: EventService,
    initialState: KClass<out ControllerState> = ControllerStateGame::class,
) : KtxInputAdapter, EventListener {

    private val commandState = booleanArrayOf(*Command.entries.map { false }.toBooleanArray())
    private val gameState = ControllerStateGame(eventService)
    private val uiState = ControllerStateUI(eventService)
    private var activeState: ControllerState = initialActiveState(initialState)
        set(value) {
            for (i in commandState.indices) {
                commandState[i] = false
            }
            field.onInactive()
            value.onActive()
            field = value
        }

    private fun initialActiveState(initialState: KClass<out ControllerState>): ControllerState = when (initialState) {
        ControllerStateGame::class -> gameState
        ControllerStateUI::class -> uiState
        else -> gdxError("Unsupported initial state $initialState")
    }

    private val keyMapping = mapOf(
        Keys.A to Command.LEFT,
        Keys.D to Command.RIGHT,
        Keys.W to Command.UP,
        Keys.S to Command.DOWN,
        Keys.SPACE to Command.SELECT,
    )

    override fun keyDown(keycode: Int): Boolean {
        keyMapping[keycode]?.let { cmd ->
            commandState[cmd] = true
            activeState.keyDown(cmd)
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        keyMapping[keycode]?.let { cmd ->
            if (!commandState[cmd]) {
                // button was not pressed before in the current state -> ignore it
                return false
            }

            commandState[cmd.ordinal] = false
            activeState.keyUp(cmd)
            return true
        }
        return false
    }

    override fun onEvent(event: Event) {
        when (event) {
            is DialogBeginEvent -> activeState = uiState
            is DialogEndEvent -> activeState = gameState
            else -> Unit
        }
    }

    private operator fun BooleanArray.get(command: Command) = this[command.ordinal]

    private operator fun BooleanArray.set(command: Command, value: Boolean) {
        this[command.ordinal] = value
    }

}
