package io.github.masamune.screen

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.Masamune
import io.github.masamune.PhysicContactHandler
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.event.EventService
import io.github.masamune.input.KeyboardController
import io.github.masamune.system.*
import io.github.masamune.tiledmap.TiledService
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.log.logger

class GameScreen(
    private val masamune: Masamune,
    private val batch: Batch = masamune.batch,
    private val inputProcessor: InputMultiplexer = masamune.inputProcessor,
    private val eventService: EventService = masamune.event,
    private val tiledService: TiledService = masamune.tiled,
) : KtxScreen {
    // game view
    private val gameViewport: Viewport = ExtendViewport(16f, 9f)

    // physic world
    private val physicWorld = createWorld(Vector2.Zero, true).apply {
        autoClearForces = false
    }

    // ecs world
    private val world = gameWorld()

    private fun gameWorld() = configureWorld {
        injectables {
            add(batch)
            add(gameViewport)
            add(physicWorld)
        }

        systems {
            add(MoveSystem())
            add(PhysicSystem())
            add(PlayerInteractSystem())
            add(CameraSystem())
            add(AnimationSystem())
            add(RenderSystem())
        }
    }

    override fun show() {
        inputProcessor.clear()
        inputProcessor.addProcessor(KeyboardController(eventService))
        eventService += world
        physicWorld.setContactListener(PhysicContactHandler(eventService, world))
        tiledService.setMap(TiledMapAsset.VILLAGE, world)
    }

    override fun hide() {
        eventService -= world
        physicWorld.setContactListener(null)
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        world.update(delta)
    }

    override fun dispose() {
        log.debug { "Disposing world with '${world.numEntities}' entities" }
        world.dispose()
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
