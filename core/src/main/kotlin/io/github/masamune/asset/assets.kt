package io.github.masamune.asset

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Disposable
import ktx.assets.disposeSafely
import ktx.assets.getAsset
import ktx.assets.load
import ktx.log.logger

enum class AtlasAsset {
    CHARACTERS,
    SFX;

    val path = "graphics/${name.lowercase()}.atlas"
}

class AssetService : Disposable {

    private val manager = AssetManager()

    fun load(asset: AtlasAsset) {
        manager.load<TextureAtlas>(asset.path)
    }

    operator fun get(asset: AtlasAsset): TextureAtlas = manager.getAsset<TextureAtlas>(asset.path)

    fun update(): Boolean = manager.update(1 / 60 * 1000)

    override fun dispose() {
        log.debug { "Disposing AssetService:\n${manager.diagnostics}" }
        manager.disposeSafely()
    }

    companion object {
        private val log = logger<AssetService>()
    }
}
