package io.github.masamune.tiledmap

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.Masamune.Companion.UNIT_SCALE
import io.github.masamune.component.MoveTo
import io.github.masamune.component.Portal
import io.github.masamune.component.Teleport
import io.github.masamune.component.Transform
import io.github.masamune.event.MapTransitionBeginEvent
import io.github.masamune.event.MapTransitionEndEvent
import io.github.masamune.tiledmap.TiledService.Companion.portal
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.width
import ktx.tiled.x
import ktx.tiled.y

interface MapTransitionService {
    val tiledService: TiledService

    fun startTransition(world: World, player: Entity, portalEntity: Entity)
    fun update(world: World, deltaTime: Float)
}

enum class MapTransitionType {
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP,
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
}

class ImmediateMapTransitionService(
    override val tiledService: TiledService,
) : MapTransitionService {

    override fun startTransition(world: World, player: Entity, portalEntity: Entity) = with(world) {
        val (toMapAsset, toPortalId) = portalEntity[Portal]
        val toTiledMap = tiledService.loadMap(toMapAsset)
        val portalMapObject = toTiledMap.portal(toPortalId)

        // unload current map and set new already loaded map
        tiledService.setMap(toTiledMap, world)

        // move player to correct location
        val x = portalMapObject.x * UNIT_SCALE
        val y = portalMapObject.y * UNIT_SCALE
        val width = portalMapObject.width * UNIT_SCALE
        val height = portalMapObject.height * UNIT_SCALE
        player.configure {
            val targetY = if (y < toTiledMap.height * 0.5f) y + 2f else y - 2f
            it += Teleport(vec2(x + width * 0.5f, targetY + height * 0.5f))
        }
    }

    override fun update(world: World, deltaTime: Float) = Unit
}

class DefaultMapTransitionService(
    override val tiledService: TiledService,
) : MapTransitionService {

    private var transitionTime = 0f
    private var nextMap = TiledMap()
    private var playerEntity = Entity.NONE
    private var transitionType = MapTransitionType.TOP_TO_BOTTOM
    private val mapOffset = vec2()

    override fun update(world: World, deltaTime: Float) {
        if (transitionTime == 0f) {
            return
        }

        transitionTime -= deltaTime
        if (transitionTime <= 0f) {
            log.debug { "Map transition finished" }

            // teleport player to correct location -> because of map transition he is currently out of bounds
            // and needs to be set to the correct location of the new map
            with(world) {
                var (x, y) = playerEntity[Transform].position
                when (transitionType) {
                    MapTransitionType.TOP_TO_BOTTOM -> {
                        x += mapOffset.x
                        y = PLAYER_MAP_EDGE_OFFSET
                    }

                    MapTransitionType.BOTTOM_TO_TOP -> {
                        x += mapOffset.x
                        y = nextMap.height - PLAYER_MAP_EDGE_OFFSET
                    }

                    MapTransitionType.LEFT_TO_RIGHT -> x = nextMap.width - PLAYER_MAP_EDGE_OFFSET
                    else -> x = PLAYER_MAP_EDGE_OFFSET
                }

                playerEntity.configure {
                    it += Teleport(vec2(x, y))
                }
            }

            // transition is finished -> update active map
            transitionTime = 0f
            tiledService.setMap(nextMap, world)
            tiledService.eventService.fire(MapTransitionEndEvent)
        }
    }

    override fun startTransition(world: World, player: Entity, portalEntity: Entity) = with(world) {
        log.debug { "Starting map transition" }

        // load new map but don't set it as active yet to not spawn any objects
        val (toMapAsset, toPortalId) = portalEntity[Portal]
        val toTiledMap = tiledService.loadMap(toMapAsset)

        // set transition time and the map that will be set at the end of the transition
        // + some additional properties that are necessary for the MapTransitionEvent
        val fromCenter = portalEntity[Transform].center()
        val fromTiledMap = tiledService.activeMap
        val portalMapObject = toTiledMap.portal(toPortalId)
        val toCenter = portalMapObject.center()
        nextMap = toTiledMap
        transitionTime = 2f
        playerEntity = player
        transitionType = transitionType(fromCenter, fromTiledMap)
        mapOffset.set(toCenter.x - fromCenter.x, toCenter.y - fromCenter.y)
        log.debug { "Transition of type $transitionType and offset $mapOffset" }
        val transitionInterpolation = Interpolation.fade
        tiledService.eventService.fire(
            MapTransitionBeginEvent(
                fromTiledMap,
                toTiledMap,
                transitionTime,
                transitionInterpolation,
                transitionType,
                mapOffset
            )
        )

        // move player to target location of new map
        // this is equal to a position outside the current active map which will be fixed at the end of the transition
        // to move the player in bounds again
        movePlayerOutOfBounds(fromTiledMap, transitionInterpolation)
    }

    private fun World.movePlayerOutOfBounds(fromTiledMap: TiledMap, interpolation: Interpolation) {
        playerEntity.configure {
            val (playerX, playerY) = it[Transform].position
            val to = when (transitionType) {
                MapTransitionType.TOP_TO_BOTTOM -> {
                    val diffY = fromTiledMap.height - playerY
                    vec2(playerX, playerY + diffY + PLAYER_MAP_EDGE_OFFSET)
                }

                MapTransitionType.BOTTOM_TO_TOP -> {
                    vec2(playerX, -PLAYER_MAP_EDGE_OFFSET)
                }

                MapTransitionType.LEFT_TO_RIGHT -> {
                    val diffX = fromTiledMap.width - playerX
                    vec2(playerX + diffX + PLAYER_MAP_EDGE_OFFSET, playerY)
                }

                else -> {
                    vec2(PLAYER_MAP_EDGE_OFFSET, playerY)
                }

            }
            it += MoveTo(to, transitionTime, interpolation)
        }
    }

    private fun transitionType(portalCenter: Vector2, portalTiledMap: TiledMap): MapTransitionType {
        return when {
            portalCenter.x < 3f -> MapTransitionType.LEFT_TO_RIGHT
            portalCenter.y < 3f -> MapTransitionType.BOTTOM_TO_TOP
            portalCenter.y > portalTiledMap.height - 3f -> MapTransitionType.TOP_TO_BOTTOM
            else -> MapTransitionType.RIGHT_TO_LEFT
        }
    }

    private fun MapObject.center(): Vector2 = vec2(
        x * UNIT_SCALE + width * 0.5f * UNIT_SCALE,
        y * UNIT_SCALE + height * 0.5f * UNIT_SCALE
    )

    companion object {
        private val log = logger<DefaultMapTransitionService>()

        // offset of player from the next map's edge
        // e.g. if player leaves a map at the top  then he will be at location (x, OFFSET) on the new map.
        private const val PLAYER_MAP_EDGE_OFFSET = 2.5f
    }

}
