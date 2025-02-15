package io.github.masamune.screen

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha
import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.masamune.Masamune
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.I18NAsset
import io.github.masamune.asset.MusicAsset
import io.github.masamune.asset.SkinAsset
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.event.EventService
import io.github.masamune.input.ControllerStateUI
import io.github.masamune.input.KeyboardController
import io.github.masamune.ui.model.MainMenuViewModel
import io.github.masamune.ui.view.mainMenuView
import ktx.actors.alpha
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import ktx.assets.toInternalFile
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.table

class MainMenuScreen(
    private val masamune: Masamune,
    private val inputProcessor: InputMultiplexer = masamune.inputProcessor,
    private val eventService: EventService = masamune.event,
    batch: Batch = masamune.batch,
    assetService: AssetService = masamune.asset,
    private val audioService: AudioService = masamune.audio,
) : KtxScreen {

    private val uiViewport = ExtendViewport(928f, 522f)
    private val stage = Stage(uiViewport, batch)
    private val skin = assetService[SkinAsset.DEFAULT]
    private val bundle: I18NBundle = assetService[I18NAsset.MESSAGES]
    private val keyboardController = KeyboardController(eventService, initialState = ControllerStateUI::class)
    private val mmViewModel = MainMenuViewModel(bundle, audioService)
    private val bgd = Texture("ui/mm_bgd.png".toInternalFile())

    override fun show() {
        // set controller
        inputProcessor.clear()
        inputProcessor.addProcessor(keyboardController)

        // setup UI views
        stage.clear()
        stage.actors {
            table(skin) {
                setFillParent(true)
                image(TextureRegionDrawable(bgd)) {
                    this.setScaling(Scaling.stretch)
                    this.alpha = 0.5f
                }
            }
            table(skin) {
                setFillParent(true)
                image(this@table.skin.getDrawable("logo")) {
                    this.setScaling(Scaling.none)
                    this += alpha(0f) + delay(1f) + fadeIn(2f, Interpolation.fastSlow)
                }
                this.top().right().padTop(50f).padRight(50f)
            }
            mainMenuView(mmViewModel, skin)
        }
        mmViewModel.startGame = false

        // register all event listeners
        eventService += stage
        eventService += keyboardController
        eventService += masamune.audio

        audioService.play(MusicAsset.FOREST)
    }

    override fun hide() {
        eventService.clearListeners()
    }

    override fun resize(width: Int, height: Int) {
        uiViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        uiViewport.apply()
        stage.act(delta)
        stage.draw()

        if (mmViewModel.startGame) {
            masamune.setScreen<GameScreen>()
            masamune.getScreen<GameScreen>().setMap(TiledMapAsset.VILLAGE)
        }
    }

    override fun dispose() {
        stage.dispose()
        bgd.dispose()
    }
}
