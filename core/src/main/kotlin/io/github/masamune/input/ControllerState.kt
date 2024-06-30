package io.github.masamune.input

import com.badlogic.gdx.math.Vector2
import io.github.masamune.event.*
import ktx.math.vec2

sealed interface ControllerState {
    fun keyDown(command: Command)
    fun keyUp(command: Command)
    fun onActive() = Unit
    fun onInactive() = Unit
}

class ControllerStateGame(private val eventService: EventService) : ControllerState {

    private val direction = vec2()

    override fun onActive() {
        direction.setZero()
    }

    override fun onInactive() {
        // stop player movement
        eventService.fire(PlayerMoveEvent(Vector2.Zero))
    }

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

class ControllerStateUI(private val eventService: EventService) : ControllerState {
    override fun keyDown(command: Command) = when (command) {
        Command.UP -> eventService.fire(UiUpEvent)
        Command.DOWN -> eventService.fire(UiDownEvent)
        Command.SELECT -> eventService.fire(UiSelectEvent)
        else -> Unit
    }

    override fun keyUp(command: Command) = Unit
}
