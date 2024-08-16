package io.github.masamune.asset

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader.TextureAtlasParameter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import io.github.masamune.component.Animation.Companion.DEFAULT_FRAME_DURATION
import io.github.masamune.component.FacingDirection
import io.github.masamune.component.GdxAnimation
import io.github.masamune.tiledmap.AnimationType
import ktx.app.gdxError
import ktx.collections.GdxArray
import ktx.log.logger
import com.badlogic.gdx.utils.StringBuilder as GdxStringBuilder

data class CachingAtlas(
    val type: AtlasAsset,
    private val textureAtlas: TextureAtlas,
) : Disposable {

    private val regionCache = mutableMapOf<String, GdxArray<TextureAtlas.AtlasRegion>>()
    private val animationCache = mutableMapOf<GdxStringBuilder, GdxAnimation>()
    private val stringBuilder = GdxStringBuilder()

    fun findRegions(name: String): GdxArray<TextureAtlas.AtlasRegion> {
        return regionCache.getOrPut(name) {
            val regions = textureAtlas.findRegions(name)
            if (regions.isEmpty) {
                gdxError("There are no regions with name $name in atlas $type")
            }
            regions
        }
    }


    fun gdxAnimation(
        mainKey: String,
        animationType: AnimationType,
        direction: FacingDirection = FacingDirection.UNDEFINED
    ): GdxAnimation {
        stringBuilder.clear()
        stringBuilder.append(mainKey)
            .append("/")
            .append(animationType.atlasKey)
        if (direction != FacingDirection.UNDEFINED) {
            stringBuilder.append("_")
                .append(direction.atlasKey)
        }

        return animationCache.getOrPut(stringBuilder) {
            val texRegions = findRegions(stringBuilder.toString())
            if (texRegions.isEmpty) {
                gdxError("No regions in atlas $type for key $stringBuilder")
            }

            GdxAnimation(DEFAULT_FRAME_DURATION, texRegions, PlayMode.LOOP)
        }
    }

    override fun dispose() {
        log.debug { "Disposing caching atlas $type with ${regionCache.size} cached regions and ${animationCache.size} cached animations" }
        textureAtlas.dispose()
    }

    companion object {
        private val log = logger<CachingAtlas>()
    }
}

class CachingAtlasParameter(
    val type: AtlasAsset,
    val flip: Boolean = false,
) : AssetLoaderParameters<CachingAtlas>()

class CachingAtlasLoader(
    fileHandleResolver: FileHandleResolver
) : SynchronousAssetLoader<CachingAtlas, CachingAtlasParameter>(fileHandleResolver) {

    private val textureAtlasLoader = TextureAtlasLoader(fileHandleResolver)

    override fun getDependencies(
        fileName: String,
        file: FileHandle,
        parameter: CachingAtlasParameter
    ): Array<AssetDescriptor<Any>> {
        return textureAtlasLoader.getDependencies(fileName, file, TextureAtlasParameter(parameter.flip))
    }

    override fun load(
        assetManager: AssetManager,
        fileName: String,
        file: FileHandle,
        parameter: CachingAtlasParameter
    ): CachingAtlas {
        val atlas = textureAtlasLoader.load(assetManager, fileName, file, TextureAtlasParameter(parameter.flip))
        return CachingAtlas(parameter.type, atlas)
    }
}
