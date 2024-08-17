package io.github.masamune.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.masamune.component.Facing
import io.github.masamune.component.FacingDirection
import io.github.masamune.component.Player
import io.github.masamune.event.Event
import io.github.masamune.event.EventListener
import io.github.masamune.event.PlayerMoveEvent

class FacingSystem : IteratingSystem(family { all(Facing) }), EventListener {
    private val playerEntities = family { all(Facing, Player) }

    override fun onTickEntity(entity: Entity) = with(entity[Facing]) {
        lastDirection = direction
    }

    override fun onEvent(event: Event) {
        if (event is PlayerMoveEvent) {
            val newDirection = when {
                event.direction.y > 0f -> FacingDirection.UP
                event.direction.y < 0f -> FacingDirection.DOWN
                event.direction.x > 0f -> FacingDirection.RIGHT
                event.direction.x < 0f -> FacingDirection.LEFT
                else -> return // no facing update needed
            }

            playerEntities.forEach { entity ->
                entity[Facing].direction = newDirection
            }
        }
    }
}
