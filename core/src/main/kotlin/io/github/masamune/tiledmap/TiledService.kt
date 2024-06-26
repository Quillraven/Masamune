package io.github.masamune.tiledmap

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Vector2
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
import ktx.math.vec3
import ktx.tiled.id
import ktx.tiled.isEmpty
import kotlin.system.measureTimeMillis

class TiledService(
    val assetService: AssetService,
    val eventService: EventService,
) {
    private var currentMap: TiledMapAsset? = null

    fun setMap(asset: TiledMapAsset, world: World) {
        val loadingTime = measureTimeMillis {
            currentMap?.let { assetService.unload(it) }
            assetService.load(asset)
            assetService.finishLoading()
        }

        log.debug { "Unloading of map $currentMap and loading of map $asset took $loadingTime ms" }
        currentMap = asset
        setMap(assetService[asset], world)
    }

    fun setMap(tiledMap: TiledMap, world: World) {
        tiledMap.spawnBoundaryBodies(world)
        loadGroundCollision(tiledMap, world)
        loadObjects(tiledMap, world)
        eventService.fire(MapChangeEvent(tiledMap))
    }

    private fun TiledMap.forEachCell(action: (cell: Cell, cellX: Int, cellY: Int) -> Unit) {
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

            cell.tile.toBody(world, cellX.toFloat(), cellY.toFloat(), BodyType.StaticBody)
        }
    }

    private fun loadObjects(tiledMap: TiledMap, world: World) {
        tiledMap.layers
            // filter for object layers
            .filter { it::class == MapLayer::class }
            .map { it.objects }
            .forEach { mapObjects ->
                mapObjects.forEach { loadObject(it, world) }
            }
    }

    private fun loadObject(mapObject: MapObject, world: World) {
        val tiledObj = mapObject as TiledMapTileMapObject
        val tile = tiledObj.tile
        val x = tiledObj.x * UNIT_SCALE
        val y = tiledObj.y * UNIT_SCALE

        world.entity {
            configureTiled(it, tiledObj, tile)
            val texRegionSize = configureGraphic(it, tile)
            it += Transform(position = vec3(x, y, 0f), size = texRegionSize)
            configureMove(it, tile)
            configurePhysic(it, tile, world, x, y)
            configureDialog(it, tile)

            if (mapObject.name == "Player") {
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
        val atlasAsset = AtlasAsset.valueOf(atlasStr)
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

        val body = tile.toBody(world, objX, objY, bodyType, entity)
        entity += Physic(body)
    }

    private fun EntityCreateContext.configureDialog(entity: Entity, tile: TiledMapTile) {
        if (tile.dialogName.isBlank()) {
            return
        }

        entity += Dialog(tile.dialogName)
    }

    private fun EntityCreateContext.configurePlayer(entity: Entity) {
        log.debug { "Configuring player" }
        entity += Tag.CAMERA_FOCUS
        entity += Player("Alexxius")
        entity += Interact()
    }

    companion object {
        private val log = logger<TiledService>()
    }

}
