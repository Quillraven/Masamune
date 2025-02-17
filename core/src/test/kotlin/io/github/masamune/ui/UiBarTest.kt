package io.github.masamune.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.masamune.gdxTest
import io.github.masamune.ui.widget.Bar
import io.github.masamune.ui.widget.bar
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.toClasspathFile
import ktx.math.vec2
import ktx.scene2d.actors

/**
 * UI test for the custom [bar] widget.
 * It shows a tinted bar version when moving the bar to the left/right.
 */

fun main() = gdxTest("UI Bar Test; 1,2,3,4 to adjust bar value", UiBarTest(), windowSize = vec2(300f, 300f))

private class UiBarTest : KtxApplicationAdapter {
    private val uiViewport = ExtendViewport(300f, 300f)
    private val batch by lazy { SpriteBatch() }
    private val stage by lazy { Stage(uiViewport, batch) }
    private val uiAtlas by lazy { TextureAtlas("ui/skin.atlas".toClasspathFile()) }
    private val skin by lazy { Skin("ui/skin.json".toClasspathFile(), uiAtlas) }
    private lateinit var bar: Bar

    override fun create() {
        stage.actors {
            this@UiBarTest.bar = bar(skin, 0.5f, 0f, 1f, 0.01f, skin.getColor("white")) {
                this.setPosition(50f, 50f)
                this.setSize(200f, 50f)
            }
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
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> bar.value = 0f
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) -> bar.value = 0.25f
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) -> bar.value = 0.75f
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) -> bar.value = 1f
        }
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        skin.dispose()
    }
}
