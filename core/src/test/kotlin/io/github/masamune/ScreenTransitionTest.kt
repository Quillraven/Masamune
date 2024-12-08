package io.github.masamune

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.StretchViewport
import io.github.masamune.screen.BlurTransitionType
import io.github.masamune.screen.TransitionType
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.toClasspathFile
import ktx.graphics.use
import ktx.log.Logger
import ktx.math.vec2

private val log = Logger("TransitionTest")

fun main() = gdxTest("Screen Transition Test", TransitionTest(), windowSize = vec2(300f, 300f))

private class Screen1(private val batch: Batch, private val texture: Texture) : KtxScreen {
    private val viewport = StretchViewport(32f, 32f)

    override fun show() {
        log.debug { "Screen 1 show" }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        log.debug { "Resize Screen 1" }
    }

    override fun render(delta: Float) {
        log.debug { "Render Screen 1" }
        viewport.apply()
        batch.use(viewport.camera) {
            it.setColor(1f, 0f, 0f, 1f)
            it.draw(texture, 0f, 0f)
        }
    }
}

private class Screen2(private val batch: Batch, private val texture: Texture) : KtxScreen {
    private val viewport = StretchViewport(32f, 32f)

    override fun show() {
        log.debug { "Screen 2 show" }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        log.debug { "Resize Screen 2" }
    }

    override fun render(delta: Float) {
        log.debug { "Render Screen 2" }
        viewport.apply()
        batch.use(viewport.camera) {
            it.setColor(0f, 1f, 0f, 1f)
            it.draw(texture, 16f, 16f)
        }
    }
}

private class TransitionTest : KtxGame<KtxScreen>() {
    private val serviceLocator by lazy { LazyServiceLocator() }
    private val texture by lazy { Texture("hero.png".toClasspathFile()) }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        addScreen(Screen1(serviceLocator.batch, texture))
        addScreen(Screen2(serviceLocator.batch, texture))
        setScreen<Screen1>()

        serviceLocator.shader.loadAllShader()

        transitionScreen<Screen2>(BlurTransitionType(0f, 6f, 2f), BlurTransitionType(6f, 0f, 2f))
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        serviceLocator.shader.resize(width, height)
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        val deltaTime = Gdx.graphics.deltaTime.coerceAtMost(1 / 30f)

        if (serviceLocator.screenTransition.hasActiveTransition) {
            serviceLocator.screenTransition.render(deltaTime)
        }

        when {
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> {
                log.debug { "Transition Screen 1" }
                transitionScreen<Screen1>(BlurTransitionType(0f, 6f, 2f), BlurTransitionType(6f, 0f, 2f))
            }

            Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) -> {
                log.debug { "Transition Screen 2" }
                transitionScreen<Screen2>(BlurTransitionType(0f, 6f, 2f), BlurTransitionType(6f, 0f, 2f))
            }
        }
    }

    inline fun <reified T : KtxScreen> transitionScreen(
        fromType: TransitionType,
        toType: TransitionType,
    ) {
        val toScreen = getScreen<T>()
        toScreen.resize(Gdx.graphics.width, Gdx.graphics.height)
        serviceLocator.screenTransition.transition(shownScreen, fromType, toScreen, toType) {
            setScreen<T>()
        }
    }

    override fun dispose() {
        super.dispose()
        serviceLocator.dispose()
        texture.dispose()
    }
}
