package io.github.masamune

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.masamune.event.EventService
import io.github.masamune.ui.model.DialogViewModel
import io.github.masamune.ui.view.DialogView
import io.github.masamune.ui.view.dialogView
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.toClasspathFile
import ktx.scene2d.actors

/**
 * Test for [DialogView].
 * It loads a dialog with an image, caption, content and three options. The test contains
 * four different versions that can be changed by pressing:
 * - '1': dialog with a lot of content, large caption text and multiple options
 * - '2': dialog with small content, caption and just one option
 * - '3': dialog with image and no caption
 * - '4': dialog without image
 */

fun main() = gdxTest("UI Dialog Test; 1-4=dialogs, W/S=change option", UiDialogTest())

private class UiDialogTest : KtxApplicationAdapter {
    private val uiViewport = FitViewport(928f, 522f)
    private val batch by lazy { SpriteBatch() }
    private val stage by lazy { Stage(uiViewport, batch) }
    private val uiAtlas by lazy { TextureAtlas("ui/skin.atlas".toClasspathFile()) }
    private val skin by lazy { Skin("ui/skin.json".toClasspathFile(), uiAtlas) }
    private lateinit var dialogView: DialogView

    override fun create() {
        stage.actors {
            this@UiDialogTest.dialogView = dialogView(DialogViewModel(EventService()), skin)
        }
        stage.isDebugAll = DEBUG_STAGE

        loadBigDialog()
    }

    private fun loadBigDialog() {
        val contentStr = """
            Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
            sed diam nonumy eirmod tempor invidunt ut labore et dolore
            magna aliquyam erat, sed diam voluptua. At vero eos et accusam
            et justo duo dolores et ea rebum. Stet clita kasd gubergren.
        """.trimIndent()

        dialogView.run {
            text(contentStr)
            image("elder", "Flower Girl")
            clearOptions()
            option("Option 1")
            option("Long Option 2")
            option("Short 3")
        }
    }

    private fun loadSmallDialog() {
        val contentStr = "Lorem ipsum dolor sit amet"

        dialogView.run {
            text(contentStr)
            image("elder", "Elder")
            clearOptions()
            option("Option 1")
        }
    }

    private fun loadNoCaptionDialog() {
        dialogView.run {
            text("Hello no caption")
            image("elder")
            clearOptions()
            option("Option")
        }
    }

    private fun loadNoImageDialog() {
        dialogView.run {
            text("Hello no image")
            image("")
            clearOptions()
            option("Option")
        }
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
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> loadBigDialog()
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) -> loadSmallDialog()
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) -> loadNoCaptionDialog()
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) -> loadNoImageDialog()
            Gdx.input.isKeyJustPressed(Input.Keys.W) -> this@UiDialogTest.dialogView.prevOption()
            Gdx.input.isKeyJustPressed(Input.Keys.S) -> this@UiDialogTest.dialogView.nextOption()
        }
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        skin.dispose()
    }

    companion object {
        private const val DEBUG_STAGE = false
    }
}
