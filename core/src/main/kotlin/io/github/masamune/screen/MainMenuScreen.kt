package io.github.masamune.screen

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import io.github.masamune.Masamune
import io.github.masamune.Masamune.Companion.uiViewport
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.I18NAsset
import io.github.masamune.asset.MusicAsset
import io.github.masamune.asset.ShaderService
import io.github.masamune.asset.SkinAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.event.EventService
import io.github.masamune.input.ControllerStateUI
import io.github.masamune.input.KeyboardController
import io.github.masamune.ui.model.MainMenuViewModel
import io.github.masamune.ui.view.mainMenuView
import ktx.app.KtxScreen
import ktx.assets.toInternalFile
import ktx.graphics.use
import ktx.scene2d.actors

class MainMenuScreen(
    private val masamune: Masamune,
    private val inputProcessor: InputMultiplexer = masamune.inputProcessor,
    private val eventService: EventService = masamune.event,
    private val batch: Batch = masamune.batch,
    assetService: AssetService = masamune.asset,
    private val audioService: AudioService = masamune.audio,
    private val shaderService: ShaderService = masamune.shader,
) : KtxScreen {

    private val uiViewport = uiViewport()
    private val stage = Stage(uiViewport, batch)
    private val skin = assetService[SkinAsset.DEFAULT]
    private val bundle: I18NBundle = assetService[I18NAsset.MESSAGES]
    private val keyboardController = KeyboardController(eventService, initialState = ControllerStateUI::class)
    private val logo = Texture("ui/logo.png".toInternalFile())

    private var logoDelay = 2f
    private var logoAlpha = 0f
    private val logoInterpolation = Interpolation.swingOut
    private var logoTime = 0f
    private val logoFlashColor = Color(1f, 1f, 1f, 0f)

    override fun show() {
        // set controller
        inputProcessor.clear()
        inputProcessor.addProcessor(keyboardController)

        // load audio settings
        masamune.save.loadAudioSettings(audioService)

        // setup UI views
        stage.clear()
        stage.actors {
            mainMenuView(
                MainMenuViewModel(
                    this@MainMenuScreen.bundle,
                    this@MainMenuScreen.audioService,
                    this@MainMenuScreen.masamune
                ),
                masamune.webLauncher,
                skin
            )
        }

        // register all event listeners
        eventService += stage
        eventService += keyboardController
        eventService += masamune.audio

        audioService.play(MusicAsset.FOREST)

        // logo fade in effect
        logoDelay = 2f
        logoAlpha = 0f
        logoTime = 0f
    }

    override fun hide() {
        logoDelay = 2f
        eventService.clearListeners()
        audioService.stopMusic()
        masamune.save.saveAudioSettings(audioService)
    }

    override fun resize(width: Int, height: Int) {
        uiViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        uiViewport.apply()

        // render logo
        logoDelay = (logoDelay - delta).coerceAtLeast(0f)
        if (logoDelay <= 0f) {
            logoTime = (logoTime + delta * 0.25f).coerceAtMost(1f)
            logoAlpha = logoInterpolation.apply(0.2f, 1f, logoTime)
            logoFlashColor.a = logoAlpha
            shaderService.useFlashShader(batch, logoFlashColor, 1f - logoTime) {
                batch.use(uiViewport.camera) {
                    it.draw(logo, 200f, 130f, 400f, 400f)
                }
            }
        }

        batch.color = Color.WHITE
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        masamune.save.saveAudioSettings(audioService)
        stage.dispose()
        logo.dispose()
    }
}
