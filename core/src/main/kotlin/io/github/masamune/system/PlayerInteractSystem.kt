package io.github.masamune.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.atan2Deg360
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.masamune.component.Interact
import io.github.masamune.component.Tag
import io.github.masamune.component.Transform
import io.github.masamune.event.*
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2
import kotlin.math.abs

class PlayerInteractSystem : IteratingSystem(
    family = family { all(Interact, Tag.PLAYER) },
    interval = Fixed(1 / 20f)
), EventListener {

    private val playerCenter = vec2()
    private val otherCenter = vec2()

    override fun onTickEntity(entity: Entity) = with(entity[Interact]) {
        if (entities.isEmpty() || !trigger) {
            // no entities to interact or interact input button not pressed yet
            return@with
        }

        trigger = false
        entities.firstOrNull { other -> withinDirection(entity, other, lastDirection) }
            ?.let {
                log.debug { "INTERACT" }
            }

        return@with
    }

    private fun withinDirection(player: Entity, other: Entity, lastPlayerDirection: Vector2): Boolean {
        other[Transform].centerTo(otherCenter)
        player[Transform].centerTo(playerCenter)
        val (dirX, dirY) = lastPlayerDirection

        // calculate angle between player and other entities [0..360]
        val angPlayerOther = atan2Deg360(otherCenter.y - playerCenter.y, otherCenter.x - playerCenter.x)
        // calculate player direction angle [0..360]
        val angPlayerDirection = when {
            dirY > 0f -> MathUtils.acosDeg(dirX)
            else -> 360f - MathUtils.acosDeg(dirX)
        }

        // check if other entity is within a cone of player facing
        // -> true when other entity is within a tolerance of INTERACT_RADIUS of player direction angle
        val difference = abs(angPlayerOther - angPlayerDirection)
        if (difference > 180) {
            // angles are on opposite sides of the circle
            // use circular difference instead of normal difference
            return 360 - difference <= INTERACT_ANG_TOLERANCE
        }

        // angles are on the same side of the circle
        return difference <= INTERACT_ANG_TOLERANCE
    }

    private fun onPlayerBeginInteract(player: Entity, other: Entity) {
        val (entities) = player[Interact]
        entities += other
        log.debug { "interact begin contact: ${entities.size} entities" }
    }

    private fun onPlayerEndInteract(player: Entity, other: Entity) {
        val (entities) = player[Interact]
        entities -= other
        log.debug { "interact end contact: ${entities.size} entities" }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is PlayerMoveEvent -> {
                if (!event.direction.isZero) {
                    // only update direction when player is moving to keep the last direction
                    // even if the player stops moving
                    family.forEach { it[Interact].lastDirection.set(event.direction) }
                }
            }

            is PlayerInteractEvent -> family.forEach { it[Interact].trigger = true }
            is PlayerInteractBeginContactEvent -> onPlayerBeginInteract(event.player, event.other)
            is PlayerInteractEndContactEvent -> onPlayerEndInteract(event.player, event.other)
            else -> Unit
        }
    }

    companion object {
        private val log = logger<PlayerInteractSystem>()
        private const val INTERACT_ANG_TOLERANCE = 60f
    }
}
