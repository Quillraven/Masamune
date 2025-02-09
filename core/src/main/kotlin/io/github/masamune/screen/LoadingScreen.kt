package io.github.masamune.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha
import com.badlogic.gdx.scenes.scene2d.actions.Actions.forever
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.masamune.Masamune
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.I18NAsset
import io.github.masamune.asset.ShaderService
import io.github.masamune.asset.SkinAsset
import io.github.masamune.asset.TiledMapAsset
import ktx.actors.plusAssign
import ktx.actors.then
import ktx.actors.txt
import ktx.app.KtxScreen
import ktx.scene2d.actors
import ktx.scene2d.defaultStyle
import ktx.scene2d.label
import ktx.scene2d.progressBar
import ktx.scene2d.table

class LoadingScreen(
    private val masamune: Masamune,
    private val assetService: AssetService = masamune.asset,
    private val shaderService: ShaderService = masamune.shader,
    batch: Batch = masamune.batch,
) : KtxScreen {

    private var done = false
    private val uiViewport = FitViewport(928f, 522f)
    private val stage = Stage(uiViewport, batch)
    private val progressBar by lazy { stage.root.findActor<ProgressBar>("progressBar") }
    private val infoLabel by lazy { stage.root.findActor<Label>("infoLabel") }
    private val i18n by lazy { assetService[I18NAsset.MESSAGES] }

    override fun show() {
        // load default skin to be able to render basic UI with loading bar
        assetService.load(SkinAsset.DEFAULT)
        assetService.load(I18NAsset.MESSAGES)
        assetService.finishLoading()
        val skin = assetService[SkinAsset.DEFAULT]

        setupUI(skin)

        // queue remaining assets to load
        AtlasAsset.entries
            .filterNot { it == AtlasAsset.SKIN }
            .forEach(assetService::load)
    }

    private fun setupUI(skin: Skin) {
        stage.clear()
        stage.actors {
            table(skin) {
                setFillParent(true)
                label(i18n["loading.assets"], defaultStyle, skin) { cell ->
                    this.setAlignment(Align.center)
                    this.name = "infoLabel"
                    cell.padBottom(10f).row()
                }
                progressBar(0f, 1f, 0.01f, false, "default-horizontal", skin) { cell ->
                    cell.width(350f).center()
                    this.name = "progressBar"
                }
            }
        }
    }

    override fun render(delta: Float) {
        if (!done && assetService.update()) {
            shaderService.loadAllShader()
            done = true
            infoLabel.txt = i18n["loading.done"]
            infoLabel += forever(alpha(0.2f, 1f) then alpha(1f, 1f))
        } else if (done && (Gdx.input.isKeyJustPressed(Keys.ANY_KEY) || Gdx.input.isTouched)) {
            onFinishLoading()
        }

        stage.act(delta)
        stage.draw()
        progressBar.value = assetService.progress()
    }

    private fun onFinishLoading() {
        val gameScreen = GameScreen(masamune)
        masamune.addScreen(gameScreen)
        masamune.addScreen(CombatScreen(masamune))

        masamune.removeScreen<LoadingScreen>()
        dispose()
        masamune.setScreen<GameScreen>()
        gameScreen.setMap(TiledMapAsset.VILLAGE)
    }

    override fun dispose() {
        stage.dispose()
    }
}
