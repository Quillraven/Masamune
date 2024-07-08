package io.github.masamune.system

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import io.github.masamune.Masamune.Companion.UNIT_SCALE
import io.github.masamune.component.Teleport
import io.github.masamune.event.Event
import io.github.masamune.event.EventListener
import io.github.masamune.event.PlayerPortalEvent
import io.github.masamune.tiledmap.TiledService
import ktx.log.logger
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.width
import ktx.tiled.x
import ktx.tiled.y

class PortalSystem(
    private val tiledService: TiledService = inject(),
) : IntervalSystem(interval = Fixed(1 / 20f), enabled = false), EventListener {

    override fun onTick() = Unit

    override fun onEvent(event: Event) {
        if (enabled) {
            // do not react on PlayerPortalEvent until player left entering portal area
            return
        }

        when (event) {
            is PlayerPortalEvent -> {
                val (player, _, portalMapObject, toTiledMap) = event

                // unload current map and set new already loaded map
                tiledService.setMap(toTiledMap, world)

                // move player to correct location
                val x = portalMapObject.x * UNIT_SCALE
                val y = portalMapObject.y * UNIT_SCALE
                val width = portalMapObject.width * UNIT_SCALE
                val height = portalMapObject.height * UNIT_SCALE
                player.configure {
                    log.debug { "Teleporting player to $x/$y" }
                    val targetY = if (y < toTiledMap.height * 0.5f) y + 2f else y - 2f
                    it += Teleport(vec2(x + width * 0.5f, targetY + height * 0.5f))
                }
            }

            else -> Unit
        }
    }

    companion object {
        private val log = logger<PortalSystem>()
    }

}
