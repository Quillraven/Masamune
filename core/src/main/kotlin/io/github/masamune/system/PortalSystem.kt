package io.github.masamune.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import io.github.masamune.PhysicContactHandler.Companion.testPoint
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.component.*
import io.github.masamune.event.Event
import io.github.masamune.event.EventListener
import io.github.masamune.event.PlayerPortalEvent
import io.github.masamune.tiledmap.TiledService
import ktx.app.gdxError
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2

class PortalSystem(
    private val tiledService: TiledService = inject(),
) : IntervalSystem(interval = Fixed(1 / 20f), enabled = false), EventListener {

    private val portalEntities = world.family { all(Portal) }
    private var lastEnteredPortal: Entity = Entity.NONE
    private var enteredPlayer: Entity = Entity.NONE
    private var playerCenter = vec2()

    override fun onTick() {
        enteredPlayer[Transform].centerTo(playerCenter)
        if (!lastEnteredPortal[Physic].body.testPoint(playerCenter)) {
            enabled = false
        }
    }

    override fun onEvent(event: Event) {
        if (enabled) {
            // do not react on PlayerPortalEvent until player left entering portal area
            return
        }

        when (event) {
            is PlayerPortalEvent -> {
                val (player, fromPortalEntity) = event
                val (toMapName, toPortalId) = fromPortalEntity[Portal]

                // load new map
                log.debug { "Entering portal to $toMapName" }
                val toMapAsset = TiledMapAsset.entries.firstOrNull { it.name == toMapName }
                    ?: gdxError("There is no TiledMapAsset of name $toMapName")
                tiledService.setMap(toMapAsset, world)

                // move player to correct location ...
                val toPortalEntity = portalEntities.entities.firstOrNull { entity ->
                    entity[Tiled].id == toPortalId && entity.id != fromPortalEntity.id
                } ?: gdxError("There is no portal of id $toPortalId in map $toMapName")
                val (width, height) = toPortalEntity[Transform].size
                player.configure {
                    it += Teleport(toPortalEntity[Physic].body.position.cpy().add(width * 0.5f, height * 0.5f))
                }
                // ... and don't trigger the new portal until the player left its area
                enabled = true
                enteredPlayer = player
                lastEnteredPortal = toPortalEntity
            }

            else -> Unit
        }
    }

    companion object {
        private val log = logger<PortalSystem>()
    }

}
