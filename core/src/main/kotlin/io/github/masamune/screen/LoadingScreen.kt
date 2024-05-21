package io.github.masamune.screen

import io.github.masamune.Masamune
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import ktx.app.KtxScreen
import ktx.assets.disposeSafely

class LoadingScreen(
    private val masamune: Masamune,
    private val assetService: AssetService = masamune.serviceLocator.assetService,
) : KtxScreen {

    private var done = false

    override fun show() {
        AtlasAsset.entries.forEach(assetService::load)
    }

    override fun render(delta: Float) {
        if (!done && assetService.update()) {
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
