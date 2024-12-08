package io.github.masamune.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.Masamune
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.I18NAsset
import io.github.masamune.asset.ShaderService
import io.github.masamune.asset.SkinAsset
import io.github.masamune.event.EventService
import io.github.masamune.input.KeyboardController
import ktx.app.KtxScreen
import ktx.log.logger

class CombatScreen(
    private val masamune: Masamune,
    private val batch: Batch = masamune.batch,
    private val inputProcessor: InputMultiplexer = masamune.inputProcessor,
    private val eventService: EventService = masamune.event,
    private val shaderService: ShaderService = masamune.shader,
    private val assetService: AssetService = masamune.asset,
) : KtxScreen {
    // viewports and stage
    private val gameViewport: Viewport = ExtendViewport(16f, 9f)
    private val uiViewport = ExtendViewport(928f, 522f)
    private val stage = Stage(uiViewport, batch)
    private val skin = assetService[SkinAsset.DEFAULT]

    // other stuff
    private val bundle: I18NBundle = assetService[I18NAsset.MESSAGES]
    private val keyboardController = KeyboardController(eventService)

    // ecs world
    private val world = configureWorld {}

    override fun show() {
        // set controller
        inputProcessor.clear()
        inputProcessor.addProcessor(keyboardController)

        // register all event listeners
        registerEventListeners()

        shaderService.useBlurShader(batch, 6f) {
            masamune.getScreen<GameScreen>()
        }
    }

    private fun registerEventListeners() {
        eventService += world
        eventService += stage
        eventService += keyboardController
    }

    override fun hide() {
        eventService.clearListeners()
        world.removeAll(clearRecycled = true)
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        world.update(delta)

        uiViewport.apply()
        stage.act(delta)
        stage.draw()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            masamune.transitionScreen<GameScreen>(
                fromType = DefaultTransitionType,
                toType = BlurTransitionType(startBlur = 6f, endBlur = 0f, time = 2f, endAlpha = 1f, startAlpha = 0.7f)
            )
        }
    }

    override fun dispose() {
        log.debug { "Disposing world with '${world.numEntities}' entities" }
        world.dispose()
        stage.dispose()
    }

    companion object {
        private val log = logger<CombatScreen>()
    }
}
