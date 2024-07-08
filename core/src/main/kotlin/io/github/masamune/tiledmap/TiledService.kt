package io.github.masamune.tiledmap

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.masamune.Masamune.Companion.UNIT_SCALE
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.component.*
import io.github.masamune.component.Animation.Companion.DEFAULT_FRAME_DURATION
import io.github.masamune.event.EventService
import io.github.masamune.event.MapChangeEvent
import ktx.app.gdxError
import ktx.log.logger
import ktx.math.vec2
import ktx.math.vec3
import ktx.tiled.*
import kotlin.system.measureTimeMillis

class TiledService(
    val assetService: AssetService,
    val eventService: EventService,
) {
    private var currentMap: TiledMap? = null
    private val staticCollisionBodies = mutableListOf<Body>()

    fun loadMap(asset: TiledMapAsset): TiledMap {
        val loadingTime = measureTimeMillis {
            assetService.load(asset)
            assetService.finishLoading()
        }
        log.debug { "Loading of map $asset took $loadingTime ms" }

        return assetService[asset].also {
            it.properties["tiledMapAsset"] = asset
        }
    }

    fun setMap(tiledMap: TiledMap, world: World) {
        currentMap?.let { unloadMap(it, world) }
        currentMap = tiledMap

        staticCollisionBodies += tiledMap.spawnBoundaryBody(world)
        loadGroundCollision(tiledMap, world)
        loadObjects(tiledMap, world)
        eventService.fire(MapChangeEvent(tiledMap))
    }

    private fun unloadMap(tiledMap: TiledMap, world: World) {
        val tiledMapAsset = tiledMap.property<TiledMapAsset>("tiledMapAsset")
        val unloadingTime = measureTimeMillis {
            assetService.unload(tiledMapAsset)
            assetService.finishLoading()
        }
        log.debug { "Unloading of map $tiledMapAsset took $unloadingTime ms" }

        unloadObjects(world)
    }

    private fun unloadObjects(world: World) {
        // unloading static collision bodies
        staticCollisionBodies.forEach { it.world.destroyBody(it) }
        staticCollisionBodies.clear()

        // unloading non-player entities (player entities are taken over to new map)
        val nonPlayerEntities = world.family { none(Player) }
        log.debug { "Unloading ${nonPlayerEntities.numEntities} entities" }
        nonPlayerEntities.forEach { entity ->
            entity.remove()
        }
    }

    private inline fun TiledMap.forEachCell(action: (cell: Cell, cellX: Int, cellY: Int) -> Unit) {
        layers.filterIsInstance<TiledMapTileLayer>()
            .forEach { layer ->
                for (x in 0 until layer.width) {
                    for (y in 0 until layer.height) {
                        layer.getCell(x, y)?.let { action(it, x, y) }
                    }
                }
            }
    }

    private fun loadGroundCollision(tiledMap: TiledMap, world: World) {
        tiledMap.forEachCell { cell, cellX, cellY ->
            if (cell.tile.objects.isEmpty()) {
                // no collision objects -> nothing to do
                return@forEachCell
            }

            val body = cell.tile.toBody(world, cellX.toFloat(), cellY.toFloat(), BodyType.StaticBody)
            staticCollisionBodies += body
        }
    }

    private fun loadObjects(tiledMap: TiledMap, world: World) {
        tiledMap.layers["object"]?.objects?.forEach { loadObject(it, world) }
        tiledMap.layers["trigger"]?.objects?.forEach { loadTrigger(it, world) }
        tiledMap.layers["portal"]?.objects?.forEach { loadPortal(it, world) }
    }

    private fun loadPortal(mapObject: MapObject, world: World) {
        val x = mapObject.x * UNIT_SCALE
        val y = mapObject.y * UNIT_SCALE
        val w = mapObject.width * UNIT_SCALE
        val h = mapObject.height * UNIT_SCALE
        val toMapName = mapObject.name ?: ""
        if (toMapName.isBlank()) {
            gdxError("Missing name for portal: ${mapObject.id}")
        }
        val toMapAsset = TiledMapAsset.entries.firstOrNull { it.name == toMapName }
            ?: gdxError("There is no TiledMapAsset of name $toMapName")

        world.entity { entity ->
            entity += Tiled(mapObject.id, TiledObjectType.PORTAL)
            entity += Portal(toMapAsset, mapObject.targetPortalId)
            val body = mapObject.toBody(world, x, y, data = entity)
            entity += Physic(body)
            entity += Transform(vec3(body.position, 0f), vec2(w, h))
        }
    }

    private fun loadTrigger(mapObject: MapObject, world: World) {
        val x = mapObject.x * UNIT_SCALE
        val y = mapObject.y * UNIT_SCALE
        val triggerName = mapObject.name ?: ""
        if (triggerName.isBlank()) {
            gdxError("Missing name for trigger: ${mapObject.id}")
        }

        world.entity { entity ->
            entity += Trigger(triggerName)
            val body = mapObject.toBody(world, x, y, data = entity)
            entity += Physic(body)
        }
    }

    private fun loadObject(mapObject: MapObject, world: World) {
        val tiledObj = mapObject as TiledMapTileMapObject
        val isPlayerObj = mapObject.name == "Player"
        if (isPlayerObj && world.family { all(Player) }.isNotEmpty) {
            log.debug { "Player already loaded -> ignore object" }
            return
        }

        val tile = tiledObj.tile
        val x = tiledObj.x * UNIT_SCALE
        val y = tiledObj.y * UNIT_SCALE
        log.debug { "Loading object ${mapObject.id}" }

        world.entity {
            configureTiled(it, tiledObj, tile)
            val texRegionSize = configureGraphic(it, tile)
            it += Transform(position = vec3(x, y, 0f), size = texRegionSize)
            configureMove(it, tile)
            configurePhysic(it, tile, world, x, y)
            configureDialog(it, tile)
            configureTrigger(it, tile)

            if (isPlayerObj) {
                configurePlayer(it)
            }
        }
    }

    private fun EntityCreateContext.configureTiled(
        entity: Entity,
        tiledObj: TiledMapTileMapObject,
        tile: TiledMapTile
    ) {
        val objTypeStr = tile.objType
        val objType = TiledObjectType.valueOf(objTypeStr)
        entity += Tiled(tiledObj.id, objType)
    }

    private fun EntityCreateContext.configureGraphic(entity: Entity, tile: TiledMapTile): Vector2 {
        val atlasStr = tile.atlas
        val atlasAsset = AtlasAsset.entries.firstOrNull { it.name == atlasStr }
            ?: gdxError("There is no atlas of name $atlasStr")
        val atlasRegionKey = tile.atlasRegionKey
        if (atlasRegionKey.isBlank()) {
            gdxError("Missing atlasRegionKey for tile ${tile.id}")
        }

        val texRegions = assetService[atlasAsset].findRegions(atlasRegionKey)
        if (texRegions.isEmpty) {
            gdxError("No regions in atlas $atlasStr for key $atlasRegionKey")
        }

        // optional animation component
        val graphicCmpRegion: TextureRegion = if (tile.hasAnimation) {
            // add animation component
            val gdxAnimation = GdxAnimation(DEFAULT_FRAME_DURATION, texRegions, PlayMode.LOOP)
            entity += Animation(gdxAnimation)
            // use first frame for graphic component
            gdxAnimation.getKeyFrame(0f)
        } else {
            // no animation; use region for graphic component
            texRegions.first()
        }

        // graphic component
        val color = Color.valueOf(tile.color)
        val graphicCmp = Graphic(graphicCmpRegion, color)
        entity += graphicCmp
        return graphicCmp.regionSize
    }

    private fun EntityCreateContext.configureMove(entity: Entity, tile: TiledMapTile) {
        val speed = tile.speed
        if (speed > 0f) {
            entity += Move(speed = speed)
        }
    }

    private fun EntityCreateContext.configurePhysic(
        entity: Entity,
        tile: TiledMapTile,
        world: World,
        objX: Float,
        objY: Float
    ) {
        if (tile.objects.isEmpty()) {
            // no collision objects -> nothing to do
            return
        }
        val bodyTypeStr = tile.bodyType
        if (bodyTypeStr == "UNDEFINED") {
            gdxError("Physic object without defined 'bodyType' for tile ${tile.id}")
        }
        val bodyType = BodyType.valueOf(bodyTypeStr)

        val body = tile.toBody(world, objX, objY, bodyType, data = entity)
        entity += Physic(body)
    }

    private fun EntityCreateContext.configureDialog(entity: Entity, tile: TiledMapTile) {
        if (tile.dialogName.isBlank()) {
            return
        }

        entity += Dialog(tile.dialogName)
    }

    private fun EntityCreateContext.configureTrigger(entity: Entity, tile: TiledMapTile) {
        if (tile.triggerName.isBlank()) {
            return
        }

        entity += Trigger(tile.triggerName)
    }

    private fun EntityCreateContext.configurePlayer(entity: Entity) {
        log.debug { "Configuring player" }
        entity += Tag.CAMERA_FOCUS
        entity += Player("Alexxius")
        entity += Interact()
    }

    companion object {
        private val log = logger<TiledService>()

        fun TiledMap.portal(portalId: Int): MapObject {
            return layer("portal").objects
                .firstOrNull { it.id == portalId }
                ?: gdxError("There is no portal of id $portalId")
        }
    }

}
