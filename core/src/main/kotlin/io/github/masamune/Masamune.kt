package io.github.masamune

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.masamune.screen.LoadingScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.log.logger

class Masamune : KtxGame<KtxScreen>() {

    val serviceLocator: ServiceLocator by lazy { ServiceLocator() }
    val inputProcessor: InputMultiplexer by lazy { InputMultiplexer() }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        Gdx.input.inputProcessor = inputProcessor

        addScreen(LoadingScreen(this))
        setScreen<LoadingScreen>()
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        currentScreen.render(Gdx.graphics.deltaTime.coerceAtMost(1 / 30f))
    }

    override fun dispose() {
        log.info { "Maximum sprites in batch: ${(serviceLocator.batch as SpriteBatch).maxSpritesInBatch}" }

        super.dispose()
        serviceLocator.disposeSafely()
    }

    companion object {
        private val log = logger<Masamune>()

        /**
         * pixels per meter for physic world and scaling value for tiled maps and graphics.
         */
        const val UNIT_SCALE = 1 / 16f
    }

}
