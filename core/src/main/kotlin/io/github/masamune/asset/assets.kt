package io.github.masamune.asset

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.I18NBundleLoader.I18NBundleParameter
import com.badlogic.gdx.assets.loaders.SkinLoader.SkinParameter
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.I18NBundle
import ktx.assets.disposeSafely
import ktx.assets.getAsset
import ktx.assets.load
import ktx.log.logger
import java.util.*

/**
 * Asset type for [TextureAtlas] instances. Used by [AssetService].
 */
enum class AtlasAsset(folder: String = "graphics") {
    CHARS_AND_PROPS,
    SFX,
    SKIN("ui");

    val path = "$folder/${name.lowercase()}.atlas"
}

/**
 * Asset type for [TiledMap] instances. Used by [AssetService].
 */
enum class TiledMapAsset {
    VILLAGE;

    val path = "maps/${name.lowercase()}.tmx"
}

/**
 * Asset type for [Skin] instances. Used by [AssetService].
 */
enum class SkinAsset(skinName: String, atlas: AtlasAsset) {
    DEFAULT("skin", AtlasAsset.SKIN);

    val path = "ui/$skinName.json"
    val atlasPath = atlas.path
}

/**
 * Asset type for [I18NBundle] instances. Used by [AssetService].
 */
enum class I18NAsset {
    MESSAGES;

    val path = "ui/${name.lowercase()}"
}

/**
 * Service class for asset management. It supports loading and unloading of:
 * - [AtlasAsset]
 * - [TiledMapAsset]
 * - [SkinAsset]
 * - [I18NAsset]
 *
 * Loading is done asynchronously. Call [finishLoading] to immediately load any queued assets.
 */
class AssetService(fileHandleResolver: FileHandleResolver = InternalFileHandleResolver()) : Disposable {

    private val manager = AssetManager(fileHandleResolver).apply {
        setLoader(TiledMap::class.java, TmxMapLoader(this.fileHandleResolver))
    }

    /**
     * Queues an [AtlasAsset] for loading.
     */
    fun load(asset: AtlasAsset) {
        manager.load<TextureAtlas>(asset.path)
    }

    /**
     * Gets a loaded [AtlasAsset] as [TextureAtlas].
     */
    operator fun get(asset: AtlasAsset): TextureAtlas = manager.getAsset<TextureAtlas>(asset.path)

    /**
     * Queues a [TiledMapAsset] for loading.
     */
    fun load(asset: TiledMapAsset) {
        manager.load<TiledMap>(asset.path)
    }

    /**
     * Queues a [TiledMapAsset] for unloading.
     */
    fun unload(asset: TiledMapAsset) {
        manager.unload(asset.path)
    }

    /**
     * Gets a loaded [TiledMapAsset] as [TiledMap].
     */
    operator fun get(asset: TiledMapAsset): TiledMap = manager.getAsset<TiledMap>(asset.path)

    /**
     * Queues a [SkinAsset] for loading.
     */
    fun load(asset: SkinAsset) {
        manager.load<Skin>(asset.path, SkinParameter(asset.atlasPath))
    }

    /**
     * Gets a loaded [SkinAsset] as [Skin].
     */
    operator fun get(asset: SkinAsset): Skin = manager.getAsset<Skin>(asset.path)

    /**
     * Queues a [I18NAsset] for loading.
     */
    fun load(asset: I18NAsset) {
        manager.load<I18NBundle>(asset.path, I18NBundleParameter(Locale.getDefault(), Charsets.ISO_8859_1.name()))
    }

    /**
     * Gets a loaded [I18NAsset] as [I18NBundle].
     */
    operator fun get(asset: I18NAsset): I18NBundle = manager.getAsset<I18NBundle>(asset.path)

    /**
     * Updates the loading process of any queued asset.
     */
    fun update(): Boolean = manager.update(1 / 60 * 1000)

    /**
     * Immediately finishes loading of any queued asset.
     */
    fun finishLoading() = manager.finishLoading()

    /**
     * Disposes all assets and stop all loading processes.
     */
    override fun dispose() {
        log.debug { "Disposing AssetService:\n${manager.diagnostics}" }
        manager.disposeSafely()
    }

    companion object {
        private val log = logger<AssetService>()
    }
}
