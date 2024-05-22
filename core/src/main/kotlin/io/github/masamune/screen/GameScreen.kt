package io.github.masamune.screen

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.Masamune
import io.github.masamune.event.EventService
import io.github.masamune.system.AnimationSystem
import io.github.masamune.system.RenderSystem
import ktx.app.KtxScreen
import ktx.log.logger

class GameScreen(
    private val masamune: Masamune,
    private val batch: Batch = masamune.serviceLocator.batch,
    private val eventService: EventService = masamune.serviceLocator.eventService
) : KtxScreen {
    // game view
    private val gameViewport: Viewport = ExtendViewport(16f, 9f)

    // ecs world
    private val world = gameWorld()

    private fun gameWorld() = configureWorld {
        injectables {
            add(batch)
            add(gameViewport)
        }

        systems {
            add(AnimationSystem())
            add(RenderSystem())
        }
    }

    override fun show() {
        eventService += world
    }

    override fun hide() {
        eventService -= world
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
