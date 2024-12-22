package io.github.masamune.ui.model

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.component.Transform
import io.github.masamune.event.CombatEntityManaUpdateEvent
import io.github.masamune.event.CombatEntityTakeDamageEvent
import io.github.masamune.event.CombatStartEvent
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import ktx.math.vec2

class CombatViewModel(
    bundle: I18NBundle,
    private val world: World,
    private val eventService: EventService,
    private val gameViewport: Viewport,
    private val uiViewport: Viewport,
) : ViewModel(bundle) {

    var playerLife: Pair<Float, Float> by propertyNotify(0f to 0f)
    var playerMana: Pair<Float, Float> by propertyNotify(0f to 0f)
    var playerName: String by propertyNotify("")
    var playerPosition: Vector2 by propertyNotify(vec2())

    override fun onEvent(event: Event) = with(world) {
        when (event) {
            is CombatStartEvent -> {
                val player = event.player
                val playerStats = player[Stats]
                playerLife = playerStats.life to playerStats.lifeMax
                playerMana = playerStats.mana to playerStats.manaMax
                playerName = player[Name].name

                onResize()
            }

            is CombatEntityTakeDamageEvent -> {
                if (event.entity hasNo Player) {
                    return@with
                }

                playerLife = event.life to event.maxLife
            }

            is CombatEntityManaUpdateEvent -> {
                if (event.entity hasNo Player) {
                    return@with
                }

                playerMana = event.mana to event.maxMana
            }

            else -> Unit
        }
    }

    fun onResize() = with(world) {
        world.family { all(Player) }.singleOrNull()?.let { player ->
            val playerPos = player[Transform].position
            playerPosition.set(playerPos.x, playerPos.y).toUiPosition(gameViewport, uiViewport)
            notify(CombatViewModel::playerPosition, playerPosition)
        }
    }

    companion object {
        private fun Vector2.toUiPosition(from: Viewport, to: Viewport): Vector2 {
            from.project(this)
            to.unproject(this)
            this.y = to.worldHeight - this.y
            return this
        }
    }

}
