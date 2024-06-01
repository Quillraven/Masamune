package io.github.masamune.input

import com.badlogic.gdx.Input.Keys
import io.github.masamune.event.EventService
import io.github.masamune.event.PlayerInteractEvent
import io.github.masamune.event.PlayerMoveEvent
import io.github.masamune.input.KeyboardController.Command.*
import ktx.app.KtxInputAdapter
import ktx.math.vec2

class KeyboardController(private val eventService: EventService) : KtxInputAdapter {

    enum class Command {
        MOVE_LEFT,
        MOVE_RIGHT,
        MOVE_DOWN,
        MOVE_UP,
        INTERACT
    }

    private val keyMapping = mapOf(
        Keys.A to MOVE_LEFT,
        Keys.D to MOVE_RIGHT,
        Keys.W to MOVE_UP,
        Keys.S to MOVE_DOWN,
        Keys.SPACE to INTERACT,
    )

    private val direction = vec2()

    private fun updateMove(x: Float = 0f, y: Float = 0f) {
        direction.x += x
        direction.y += y
        eventService.fire(PlayerMoveEvent(direction))
    }

    override fun keyDown(keycode: Int): Boolean {
        keyMapping[keycode]?.let { cmd ->
            when (cmd) {
                MOVE_LEFT -> updateMove(x = -1f)
                MOVE_RIGHT -> updateMove(x = 1f)
                MOVE_DOWN -> updateMove(y = -1f)
                MOVE_UP -> updateMove(y = 1f)
                INTERACT -> eventService.fire(PlayerInteractEvent)
            }
            return true
        }

        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        keyMapping[keycode]?.let { cmd ->
            when (cmd) {
                MOVE_LEFT -> updateMove(x = 1f)
                MOVE_RIGHT -> updateMove(x = -1f)
                MOVE_DOWN -> updateMove(y = 1f)
                MOVE_UP -> updateMove(y = -1f)
                else -> return true
            }
            return true
        }

        return false
    }

}
