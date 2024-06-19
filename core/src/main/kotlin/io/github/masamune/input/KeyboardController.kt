package io.github.masamune.input

import com.badlogic.gdx.Input.Keys
import io.github.masamune.event.*
import ktx.app.KtxInputAdapter
import ktx.math.vec2

private enum class Command {
    LEFT,
    RIGHT,
    DOWN,
    UP,
    SELECT
}

private sealed interface ControllerState {
    fun keyDown(command: Command)
    fun keyUp(command: Command)
}

private class ControllerStateGame(private val eventService: EventService) : ControllerState {

    private val direction = vec2()

    private fun updateMove(x: Float = 0f, y: Float = 0f) {
        direction.x += x
        direction.y += y
        eventService.fire(PlayerMoveEvent(direction))
    }

    override fun keyDown(command: Command) = when (command) {
        Command.LEFT -> updateMove(x = -1f)
        Command.RIGHT -> updateMove(x = 1f)
        Command.DOWN -> updateMove(y = -1f)
        Command.UP -> updateMove(y = 1f)
        Command.SELECT -> eventService.fire(PlayerInteractEvent)
    }

    override fun keyUp(command: Command) = when (command) {
        Command.LEFT -> updateMove(x = 1f)
        Command.RIGHT -> updateMove(x = -1f)
        Command.DOWN -> updateMove(y = 1f)
        Command.UP -> updateMove(y = -1f)
        else -> Unit
    }
}

private class ControllerStateUI(private val eventService: EventService) : ControllerState {
    override fun keyDown(command: Command) = when (command) {
        Command.UP -> eventService.fire(UiUpEvent)
        Command.DOWN -> eventService.fire(UiDownEvent)
        Command.SELECT -> eventService.fire(UiSelectEvent)
        else -> Unit
    }

    override fun keyUp(command: Command) = Unit
}

class KeyboardController(eventService: EventService) : KtxInputAdapter, EventListener {

    private val gameState = ControllerStateGame(eventService)
    private val uiState = ControllerStateUI(eventService)
    private var activeState: ControllerState = gameState

    private val keyMapping = mapOf(
        Keys.A to Command.LEFT,
        Keys.D to Command.RIGHT,
        Keys.W to Command.UP,
        Keys.S to Command.DOWN,
        Keys.SPACE to Command.SELECT,
    )

    override fun keyDown(keycode: Int): Boolean {
        keyMapping[keycode]?.let { cmd ->
            activeState.keyDown(cmd)
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        keyMapping[keycode]?.let { cmd ->
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

}
