package io.github.masamune

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.masamune.asset.AssetService
import io.github.masamune.screen.LoadingScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely

class Masamune : KtxGame<KtxScreen>() {

    val batch: Batch by lazy { SpriteBatch() }
    val assetService: AssetService by lazy { AssetService() }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        addScreen(LoadingScreen(this))
        setScreen<LoadingScreen>()
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        currentScreen.render(Gdx.graphics.deltaTime.coerceAtMost(1 / 30f))
    }

    override fun dispose() {
        super.dispose()
        batch.disposeSafely()
        assetService.disposeSafely()
    }

    companion object {
        /**
         * pixels per meter for physic world and scaling value for tiled maps and graphics.
         */
        const val UNIT_SCALE = 1 / 16f
    }

}
