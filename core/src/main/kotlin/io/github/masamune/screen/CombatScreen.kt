package io.github.masamune.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.HdpiUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.Masamune
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.I18NAsset
import io.github.masamune.asset.MusicAsset
import io.github.masamune.asset.ShaderService
import io.github.masamune.asset.ShaderService.Companion.resize
import io.github.masamune.asset.SkinAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.event.EventService
import io.github.masamune.input.KeyboardController
import ktx.app.KtxScreen
import ktx.graphics.component1
import ktx.graphics.component2
import ktx.graphics.component3
import ktx.graphics.component4
import ktx.graphics.use
import ktx.log.logger

class CombatScreen(
    private val masamune: Masamune,
    private val batch: Batch = masamune.batch,
    private val inputProcessor: InputMultiplexer = masamune.inputProcessor,
    private val eventService: EventService = masamune.event,
    private val shaderService: ShaderService = masamune.shader,
    private val assetService: AssetService = masamune.asset,
    private val audioService: AudioService = masamune.audio,
) : KtxScreen {
    // viewports and stage
    private val gameViewport: Viewport = ExtendViewport(16f, 9f)
    private val uiViewport = ExtendViewport(928f, 522f)
    private val stage = Stage(uiViewport, batch)
    private val skin = assetService[SkinAsset.DEFAULT]

    // other stuff
    private val bundle: I18NBundle = assetService[I18NAsset.MESSAGES]
    private val keyboardController = KeyboardController(eventService)
    private var fbo = FrameBuffer(ShaderService.FBO_FORMAT, Gdx.graphics.width, Gdx.graphics.height, false)
    private var prevMusic: MusicAsset? = null

    // ecs world
    private val world = configureWorld {}

    override fun show() {
        // set controller
        inputProcessor.clear()
        inputProcessor.addProcessor(keyboardController)

        // register all event listeners
        registerEventListeners()

        updateBgdFbo(Gdx.graphics.width, Gdx.graphics.height)

        prevMusic = audioService.currentMusic
        audioService.play(MusicAsset.COMBAT1)
    }

    private fun updateBgdFbo(width: Int, height: Int) {
        shaderService.useBlurShader(batch, 6f, fbo) {
            val gameScreen = masamune.getScreen<GameScreen>()
            gameScreen.resize(width, height)
            gameScreen.render(0f)
        }
    }

    private fun registerEventListeners() {
        eventService += world
        eventService += stage
        eventService += keyboardController
        eventService += masamune.audio
    }

    override fun hide() {
        eventService.clearListeners()
        world.removeAll(clearRecycled = true)
        prevMusic?.let { audioService.play(it) }
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, true)
        fbo = fbo.resize(width, height)
        updateBgdFbo(width, height)
    }

    override fun render(delta: Float) {
        // render blurred out GameScreen as background
        HdpiUtils.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        batch.use(batch.projectionMatrix.idt()) {
            val (r, g, b, a) = batch.color
            batch.setColor(r, g, b, 0.4f)
            it.draw(fbo.colorBufferTexture, -1f, 1f, 2f, -2f)
            batch.setColor(r, g, b, a)
        }
        world.update(delta)

        uiViewport.apply()
        stage.act(delta)
        stage.draw()

        // TODO remove debug
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            masamune.transitionScreen<GameScreen>(
                fromType = DefaultTransitionType,
                toType = BlurTransitionType(startBlur = 6f, endBlur = 0f, time = 2f, endAlpha = 1f, startAlpha = 0.4f)
            )
        }
    }

    override fun dispose() {
        log.debug { "Disposing world with '${world.numEntities}' entities" }
        world.dispose()
        stage.dispose()
        fbo.dispose()
    }

    companion object {
        private val log = logger<CombatScreen>()
    }
}
