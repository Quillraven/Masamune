package io.github.masamune

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.World
import io.github.masamune.screen.LoadingScreen
import io.github.masamune.screen.TransitionType
import io.github.masamune.tiledmap.DefaultMapTransitionService
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.log.logger
import kotlin.reflect.KClass

typealias PhysicWorld = World

class Masamune(
    private val serviceLocator: ServiceLocator = LazyServiceLocator(
        // replace with ImmediateMapTransitionService to change maps without any special effect
        mapTransitionServiceInitializer = { tiled -> DefaultMapTransitionService(tiled) }
        // mapTransitionServiceInitializer = { tiled -> ImmediateMapTransitionService(tiled) }
    ),
) : KtxGame<KtxScreen>(), ServiceLocator by serviceLocator {

    val inputProcessor: InputMultiplexer by lazy { InputMultiplexer() }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        Gdx.input.inputProcessor = inputProcessor
        serviceLocator.event += serviceLocator.audio

        addScreen(LoadingScreen(this))
        setScreen<LoadingScreen>()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        shader.resize(width, height)
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        val deltaTime = Gdx.graphics.deltaTime.coerceAtMost(1 / 30f)

        if (screenTransition.hasActiveTransition) {
            screenTransition.render(deltaTime)
        } else {
            currentScreen.render(deltaTime)
        }
    }

    inline fun <reified T : KtxScreen> transitionScreen(
        fromType: TransitionType,
        toType: TransitionType,
        fromFirst: Boolean = true,
    ) {
        val toScreen = getScreen<T>()
        toScreen.resize(Gdx.graphics.width, Gdx.graphics.height)
        screenTransition.transition(shownScreen, fromType, toScreen, toType, fromFirst) {
            setScreen<T>()
        }
    }

    override fun dispose() {
        log.info { "Maximum sprites in batch: ${(serviceLocator.batch as SpriteBatch).maxSpritesInBatch}" }

        super.dispose()
        serviceLocator.dispose()
    }

    companion object {
        private val log = logger<Masamune>()

        /**
         * pixels per meter for physic world and scaling value for tiled maps and graphics.
         */
        const val UNIT_SCALE = 1 / 16f
    }

}
