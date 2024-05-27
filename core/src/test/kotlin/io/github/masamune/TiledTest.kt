package io.github.masamune

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.component.Animation
import io.github.masamune.component.Graphic
import io.github.masamune.component.Tiled
import io.github.masamune.component.Transform
import io.github.masamune.event.EventService
import io.github.masamune.system.AnimationSystem
import io.github.masamune.system.DebugPhysicRenderSystem
import io.github.masamune.system.RenderSystem
import io.github.masamune.tiledmap.TiledObjectType
import io.github.masamune.tiledmap.TiledService
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun main() {
    Lwjgl3Application(TiledTest(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("Tiled Test")
        setWindowedMode(1280, 960)
    })
}

class TiledTest : KtxApplicationAdapter {
    private val batch: Batch by lazy { SpriteBatch() }
    private val gameViewport: Viewport = ExtendViewport(16f, 9f)
    private val world by lazy { gameWorld() }
    private val assetService by lazy { AssetService(ClasspathFileHandleResolver()) }
    private val eventService by lazy { EventService() }
    private val tiledService by lazy { TiledService(assetService, eventService) }
    private lateinit var tiledMap: TiledMap

    private fun gameWorld() = configureWorld {
        injectables {
            add(batch)
            add(gameViewport)
            add(createWorld(gravity = Vector2.Zero))
        }

        systems {
            add(AnimationSystem())
            add(RenderSystem())
            add(DebugPhysicRenderSystem())
        }
    }

    override fun create() {
        eventService += world

        assetService.load(AtlasAsset.CHARS_AND_PROPS)
        assetService.finishLoading()
        tiledMap = TmxMapLoader(ClasspathFileHandleResolver()).load("maps/test.tmx")
        tiledService.setMap(tiledMap, world)

        // assertions
        with(world) {
            assertEquals(2, numEntities)
            asEntityBag().forEach { entity ->
                assertTrue(entity has Tiled)
                assertTrue(entity has Transform)
                assertTrue(entity has Graphic)
                if (entity[Tiled].objType == TiledObjectType.HERO) {
                    assertTrue(entity has Animation)
                }
            }
        }

        (gameViewport.camera as OrthographicCamera).zoom = 2f
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)

        val deltaTime = Gdx.graphics.deltaTime.coerceAtMost(1 / 30f)
        world.update(deltaTime)
    }

    override fun dispose() {
        batch.disposeSafely()
        world.dispose()
        assetService.disposeSafely()
        tiledMap.disposeSafely()
    }

}
