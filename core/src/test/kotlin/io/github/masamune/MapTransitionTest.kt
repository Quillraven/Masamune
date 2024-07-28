package io.github.masamune

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.ShaderService
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.dialog.DialogConfigurator
import io.github.masamune.event.EventService
import io.github.masamune.input.KeyboardController
import io.github.masamune.system.*
import io.github.masamune.tiledmap.DefaultMapTransitionService
import io.github.masamune.tiledmap.MapTransitionService
import io.github.masamune.tiledmap.TiledService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.box2d.createWorld

fun main() {
    // we misuse the VILLAGE constant for this test to avoid adding the test maps to our
    // TiledMapAsset enum. By mocking the VILLAGE's path value we can transition to an arbitrary map.
    mockkObject(TiledMapAsset.VILLAGE)
    every { TiledMapAsset.VILLAGE.path } returns "maps/transition_test_small.tmx"

    gdxTest("Map Transition Test", MapTransitionTest())
}

private class MapTransitionTest : KtxApplicationAdapter {
    private val batch: Batch by lazy { SpriteBatch() }
    private val gameViewport: Viewport = ExtendViewport(16f, 9f)
    private val physicWorld = createWorld(gravity = Vector2.Zero)
    private val world by lazy { gameWorld() }
    private val assetService by lazy { AssetService(ClasspathFileHandleResolver()) }
    private val eventService by lazy { EventService() }
    private val tiledService by lazy { TiledService(assetService, eventService) }
    private val mapTransitionService by lazy { DefaultMapTransitionService(tiledService) }
    private lateinit var tiledMap: TiledMap

    private fun gameWorld() = configureWorld {
        injectables {
            add(batch)
            add(gameViewport)
            add(physicWorld)
            add(ShaderService())
            add(eventService)
            add(tiledService)
            add(mockk<DialogConfigurator>())
            add<MapTransitionService>(mapTransitionService)
        }

        systems {
            add(MoveSystem())
            add(MoveToSystem())
            add(PhysicSystem())
            add(PlayerInteractSystem())
            add(TeleportSystem())
            add(CameraSystem())
            add(AnimationSystem())
            add(FadeSystem())
            add(RenderSystem())
            add(DebugPhysicRenderSystem())
        }
    }

    override fun create() {
        eventService += world

        physicWorld.setContactListener(PhysicContactHandler(eventService, world))

        assetService.load(AtlasAsset.CHARS_AND_PROPS)
        assetService.finishLoading()
        tiledMap = tiledService.loadMap(TiledMapAsset.VILLAGE)
        tiledService.setMap(tiledMap, world, fadeIn = false)

        Gdx.input.inputProcessor = KeyboardController(eventService)
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)

        val deltaTime = Gdx.graphics.deltaTime.coerceAtMost(1 / 30f)
        world.update(deltaTime)
        mapTransitionService.update(world, deltaTime)

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            every { TiledMapAsset.VILLAGE.path } returns "maps/transition_test_small.tmx"
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            every { TiledMapAsset.VILLAGE.path } returns "maps/transition_test_large.tmx"
        }
    }

    override fun dispose() {
        batch.dispose()
        world.dispose()
        assetService.dispose()
        tiledMap.dispose()
        physicWorld.dispose()
    }

}
