package io.github.masamune.tiledmap

import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.github.quillraven.fleks.World
import io.github.masamune.Masamune.Companion.UNIT_SCALE
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.component.Graphic
import io.github.masamune.component.Transform
import ktx.log.logger
import ktx.math.vec3
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
        val tiledMap = assetService[asset]

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

        val objTypeStr = tiledObj.tile.objType
        val objType = TiledObjectType.valueOf(objTypeStr)
        val atlasStr = tiledObj.tile.atlas
        val atlas = AtlasAsset.valueOf(atlasStr)
        val atlasRegionKey = tiledObj.tile.atlasRegionKey

        world.entity {
            val texRegion = assetService[atlas].findRegion(atlasRegionKey)
            val graphicCmp = Graphic(texRegion)
            it += graphicCmp
            it += Transform(
                position = vec3(mapObject.x * UNIT_SCALE, mapObject.y * UNIT_SCALE, 0f),
                size = graphicCmp.regionSize
            )
        }
    }

    companion object {
        private val log = logger<TiledService>()
    }

}
