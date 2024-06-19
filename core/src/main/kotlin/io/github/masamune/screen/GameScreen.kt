package io.github.masamune.screen

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.Masamune
import io.github.masamune.PhysicContactHandler
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.ShaderService
import io.github.masamune.asset.SkinAsset
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.event.EventService
import io.github.masamune.input.KeyboardController
import io.github.masamune.system.*
import io.github.masamune.tiledmap.TiledService
import io.github.masamune.ui.model.DialogViewModel
import io.github.masamune.ui.view.DialogView
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.log.logger

class GameScreen(
    private val masamune: Masamune,
    private val batch: Batch = masamune.batch,
    private val inputProcessor: InputMultiplexer = masamune.inputProcessor,
    private val eventService: EventService = masamune.event,
    private val tiledService: TiledService = masamune.tiled,
    private val shaderService: ShaderService = masamune.shader,
    assetService: AssetService = masamune.asset,
) : KtxScreen {
    // viewports and stage
    private val gameViewport: Viewport = ExtendViewport(16f, 9f)
    private val uiViewport = FitViewport(928f, 522f)
    private val stage = Stage(uiViewport, batch)
    private val skin = assetService[SkinAsset.DEFAULT]

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
            add(shaderService)
            add(eventService)
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
        val keyboardController = KeyboardController(eventService)
        inputProcessor.addProcessor(keyboardController)
        eventService += world
        eventService += keyboardController
        physicWorld.setContactListener(PhysicContactHandler(eventService, world))
        tiledService.setMap(TiledMapAsset.VILLAGE, world)

        stage.clear()
        val dialogViewModel = DialogViewModel(eventService)
        eventService += dialogViewModel
        stage.addActor(DialogView(dialogViewModel, skin))
    }

    override fun hide() {
        eventService -= world
        physicWorld.setContactListener(null)
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        world.update(delta)

        uiViewport.apply()
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        log.debug { "Disposing world with '${world.numEntities}' entities" }
        world.dispose()
        physicWorld.disposeSafely()
        stage.disposeSafely()
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
