package io.github.masamune.tiledmap

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.masamune.Masamune.Companion.UNIT_SCALE
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.component.*
import io.github.masamune.component.Animation.Companion.DEFAULT_FRAME_DURATION
import ktx.app.gdxError
import ktx.log.logger
import ktx.math.vec3
import ktx.tiled.id
import kotlin.system.measureTimeMillis

class TiledService(private val assetService: AssetService) {
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
        loadObjects(tiledMap, world)
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

    companion object {
        private val log = logger<TiledService>()
    }

}
