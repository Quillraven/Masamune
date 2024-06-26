package io.github.masamune.screen

import io.github.masamune.Masamune
import io.github.masamune.asset.*
import ktx.app.KtxScreen
import ktx.assets.disposeSafely

class LoadingScreen(
    private val masamune: Masamune,
    private val assetService: AssetService = masamune.asset,
    private val shaderService: ShaderService = masamune.shader
) : KtxScreen {

    private var done = false

    override fun show() {
        // load default skin to be able to render basic UI with loading bar
        assetService.load(SkinAsset.DEFAULT)
        assetService.load(I18NAsset.MESSAGES)
        assetService.finishLoading()

        // queue remaining assets to load
        AtlasAsset.entries
            .filterNot { it == AtlasAsset.SKIN }
            .forEach(assetService::load)
    }

    override fun render(delta: Float) {
        if (!done && assetService.update()) {
            shaderService.loadAllShader()
            done = true
            onFinishLoading()
        }
    }

    private fun onFinishLoading() {
        masamune.addScreen(GameScreen(masamune))
        masamune.removeScreen<LoadingScreen>()
        disposeSafely()
        masamune.setScreen<GameScreen>()
    }

}
