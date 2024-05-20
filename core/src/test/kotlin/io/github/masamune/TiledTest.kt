package io.github.masamune

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.component.Animation
import io.github.masamune.component.Graphic
import io.github.masamune.component.Tiled
import io.github.masamune.component.Transform
import io.github.masamune.system.AnimationSystem
import io.github.masamune.system.RenderSystem
import io.github.masamune.tiledmap.TiledObjectType
import io.github.masamune.tiledmap.TiledService
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.disposeSafely
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
    private val tiledService by lazy { TiledService(assetService) }
    private lateinit var tiledMap: TiledMap

    private fun gameWorld() = configureWorld {
        injectables {
            add(batch)
            add(gameViewport)
        }

        systems {
            add(RenderSystem())
            add(AnimationSystem())
        }
    }

    override fun create() {
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
