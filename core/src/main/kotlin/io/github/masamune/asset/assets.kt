package io.github.masamune.asset

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.utils.Disposable
import ktx.assets.disposeSafely
import ktx.assets.getAsset
import ktx.assets.load
import ktx.log.logger

enum class AtlasAsset {
    CHARS_AND_PROPS,
    SFX;

    val path = "graphics/${name.lowercase()}.atlas"
}

enum class TiledMapAsset {
    TEST;

    val path = "maps/${name.lowercase()}.tmx"
}

class AssetService : Disposable {

    private val manager = AssetManager().apply {
        setLoader(TiledMap::class.java, TmxMapLoader(this.fileHandleResolver))
    }

    fun load(asset: AtlasAsset) {
        manager.load<TextureAtlas>(asset.path)
    }

    operator fun get(asset: AtlasAsset): TextureAtlas = manager.getAsset<TextureAtlas>(asset.path)

    fun load(asset: TiledMapAsset) {
        manager.load<TiledMap>(asset.path)
    }

    fun unload(asset: TiledMapAsset) {
        manager.unload(asset.path)
    }

    operator fun get(asset: TiledMapAsset): TiledMap = manager.getAsset<TiledMap>(asset.path)

    fun update(): Boolean = manager.update(1 / 60 * 1000)

    fun finishLoading() = manager.finishLoading()

    override fun dispose() {
        log.debug { "Disposing AssetService:\n${manager.diagnostics}" }
        manager.disposeSafely()
    }

    companion object {
        private val log = logger<AssetService>()
    }
}
