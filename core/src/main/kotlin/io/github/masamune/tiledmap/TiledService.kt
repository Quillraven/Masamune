package io.github.masamune.tiledmap

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.masamune.Masamune.Companion.UNIT_SCALE
import io.github.masamune.PhysicWorld
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.component.*
import io.github.masamune.component.Animation.Companion.DEFAULT_FRAME_DURATION
import io.github.masamune.event.EventService
import io.github.masamune.event.MapChangeEvent
import ktx.app.gdxError
import ktx.box2d.body
import ktx.log.logger
import ktx.math.*
import ktx.tiled.id
import ktx.tiled.isEmpty
import ktx.tiled.x
import ktx.tiled.y
import kotlin.system.measureTimeMillis

class TiledService(
    private val assetService: AssetService,
    private val eventService: EventService,
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
        loadObjects(tiledMap, world)
        eventService.fire(MapChangeEvent(tiledMap))
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
        val physicWorld = world.inject<PhysicWorld>()

        world.entity {
            configureTiled(it, tiledObj, tile)
            val texRegionSize = configureGraphic(it, tile)
            it += Transform(position = vec3(x, y, 0f), size = texRegionSize)
            configureMove(it, tile)
            configurePhysic(it, tile, physicWorld, x, y)

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
        physicWorld: PhysicWorld,
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

        val fixtureDefs = tile.objects.map(::fixtureDefOf)
        val body = physicWorld.body(BodyType.valueOf(bodyTypeStr)) {
            position.set(objX, objY)
            fixedRotation = true
            userData = entity
        }
        fixtureDefs.forEach {
            body.createFixture(it)
            it.shape.dispose()
        }
        entity += Physic(body)
    }

    private fun EntityCreateContext.configurePlayer(entity: Entity) {
        log.debug { "Configuring player" }
        entity += listOf(Tag.PLAYER, Tag.CAMERA_FOCUS)
    }

    private fun fixtureDefOf(mapObject: MapObject): FixtureDef {
        val fixtureDef = when (mapObject) {
            is RectangleMapObject -> rectangleFixtureDef(mapObject)
            is EllipseMapObject -> ellipseFixtureDef(mapObject)
            is PolygonMapObject -> polygonFixtureDef(mapObject)
            is PolylineMapObject -> polylineFixtureDef(mapObject)
            else -> gdxError("Unsupported MapObject $mapObject")
        }

        return fixtureDef
    }

    // box is centered around body position in Box2D, but we want to have it aligned in a way
    // that the body position is the bottom left corner of the box.
    // That's why we use a 'boxOffset' below.
    private fun rectangleFixtureDef(mapObject: RectangleMapObject): FixtureDef {
        val (rectX, rectY, rectW, rectH) = mapObject.rectangle
        val boxX = rectX * UNIT_SCALE
        val boxY = rectY * UNIT_SCALE

        val boxW = rectW * UNIT_SCALE * 0.5f
        val boxH = rectH * UNIT_SCALE * 0.5f
        return FixtureDef().apply {
            shape = PolygonShape().apply {
                setAsBox(boxW, boxH, vec2(boxX + boxW, boxY + boxH), 0f)
            }
        }
    }

    private fun ellipseFixtureDef(mapObject: EllipseMapObject): FixtureDef {
        val (x, y, w, h) = mapObject.ellipse
        val ellipseX = x * UNIT_SCALE
        val ellipseY = y * UNIT_SCALE
        val ellipseW = w * UNIT_SCALE / 2f
        val ellipseH = h * UNIT_SCALE / 2f

        return if (MathUtils.isEqual(ellipseW, ellipseH, 0.1f)) {
            // width and height are equal -> return a circle shape
            FixtureDef().apply {
                shape = CircleShape().apply {
                    position = vec2(ellipseX + ellipseW, ellipseY + ellipseH)
                    radius = ellipseW
                }
            }
        } else {
            // width and height are not equal -> return an ellipse shape (=polygon with 'numVertices' vertices)
            // PolygonShape only supports 8 vertices
            // ChainShape supports more but does not properly collide in some scenarios
            val numVertices = 8
            val angleStep = MathUtils.PI2 / numVertices
            val vertices = Array(numVertices) { vertexIdx ->
                val angle = vertexIdx * angleStep
                val offsetX = ellipseW * MathUtils.cos(angle)
                val offsetY = ellipseH * MathUtils.sin(angle)
                vec2(ellipseX + ellipseW + offsetX, ellipseY + ellipseH + offsetY)
            }

            FixtureDef().apply {
                shape = PolygonShape().apply {
                    set(vertices)
                }
            }
        }
    }

    private fun polylineFixtureDef(mapObject: PolylineMapObject): FixtureDef {
        return polygonFixtureDef(mapObject.x, mapObject.y, mapObject.polyline.vertices, false)
    }

    private fun polygonFixtureDef(mapObject: PolygonMapObject): FixtureDef {
        return polygonFixtureDef(mapObject.x, mapObject.y, mapObject.polygon.vertices, true)
    }

    private fun polygonFixtureDef(
        polyX: Float,
        polyY: Float,
        polyVertices: FloatArray,
        loop: Boolean,
    ): FixtureDef {
        val x = polyX * UNIT_SCALE
        val y = polyY * UNIT_SCALE
        val vertices = FloatArray(polyVertices.size) { vertexIdx ->
            if (vertexIdx % 2 == 0) {
                x + polyVertices[vertexIdx] * UNIT_SCALE
            } else {
                y + polyVertices[vertexIdx] * UNIT_SCALE
            }
        }

        return FixtureDef().apply {
            shape = ChainShape().apply {
                if (loop) {
                    createLoop(vertices)
                } else {
                    createChain(vertices)
                }
            }
        }
    }

    companion object {
        private val log = logger<TiledService>()
    }

}
