package io.github.masamune

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.masamune.ui.DialogWidget
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toClasspathFile

/**
 * Test for [DialogWidget].
 * It loads a dialog with an image, caption, content and option. The test contains
 * two different versions that can be changed by pressing:
 * - '1': dialog with a lot of content, large caption text and multiple options
 * - '2': dialog with small content, caption and just one option
 */

fun main() {
    Lwjgl3Application(UiDialogTest(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("UI Dialog Test; 1=big dialog, 2=small dialog")
        setWindowedMode(1280, 960)
    })
}

private class UiDialogTest : KtxApplicationAdapter {
    private val uiViewport = FitViewport(928f, 522f)
    private val batch by lazy { SpriteBatch() }
    private val stage by lazy { Stage(uiViewport, batch) }
    private val uiAtlas by lazy { TextureAtlas("ui/skin.atlas".toClasspathFile()) }
    private val skin by lazy { Skin("ui/skin.json".toClasspathFile(), uiAtlas) }

    override fun create() {
        loadBigDialog()
        Gdx.input.inputProcessor = stage
    }

    private fun loadBigDialog() {
        val contentStr = """
            {SLOW}{FADE}Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
            sed diam nonumy eirmod tempor invidunt ut labore et dolore
            magna aliquyam erat, sed diam voluptua. At vero eos et accusam
            et justo duo dolores et ea rebum. Stet clita kasd gubergren.
        """.trimIndent()

        val dialogWidget = DialogWidget(skin)
        dialogWidget.content(contentStr)
        dialogWidget.image(skin.getDrawable("elder"), "Flower Girl")
        dialogWidget.option("Option 1")
        dialogWidget.option("Option 2")
        stage.clear()
        stage.addActor(dialogWidget)
        stage.isDebugAll = false
    }

    private fun loadSmallDialog() {
        val contentStr = "{SLOW}{FADE}Lorem ipsum dolor sit amet"

        val dialogWidget = DialogWidget(skin)
        dialogWidget.content(contentStr)
        dialogWidget.image(skin.getDrawable("elder"), "Elder")
        dialogWidget.option("Option 1")
        stage.clear()
        stage.addActor(dialogWidget)
        stage.isDebugAll = false
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        uiViewport.apply()
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()

        when {
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> {
                stage.clear()
                loadBigDialog()
            }

            Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) -> {
                stage.clear()
                loadSmallDialog()
            }
        }
    }

    override fun dispose() {
        stage.disposeSafely()
        batch.disposeSafely()
        skin.disposeSafely()
    }

}
