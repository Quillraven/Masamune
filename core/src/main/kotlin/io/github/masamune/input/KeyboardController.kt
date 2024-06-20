package io.github.masamune.input

import com.badlogic.gdx.Input.Keys
import io.github.masamune.event.*
import ktx.app.KtxInputAdapter

class KeyboardController(eventService: EventService) : KtxInputAdapter, EventListener {

    private val commandState = booleanArrayOf(*Command.entries.map { false }.toBooleanArray())
    private val gameState = ControllerStateGame(eventService)
    private val uiState = ControllerStateUI(eventService)
    private var activeState: ControllerState = gameState
        set(value) {
            for (i in commandState.indices) {
                commandState[i] = false
            }
            value.onActive()
            field = value
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
