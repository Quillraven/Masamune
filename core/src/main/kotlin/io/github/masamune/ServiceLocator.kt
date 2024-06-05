package io.github.masamune

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.ShaderService
import io.github.masamune.event.EventService
import io.github.masamune.tiledmap.TiledService
import ktx.assets.disposeSafely

interface ServiceLocator : Disposable {
    val batch: Batch
    val asset: AssetService
    val event: EventService
    val tiled: TiledService
    val shader: ShaderService
}

class LazyServiceLocator(
    batchInitializer: () -> Batch = { SpriteBatch() },
    assetServiceInitializer: () -> AssetService = { AssetService() },
    eventServiceInitializer: () -> EventService = { EventService() },
    tiledServiceInitializer: (AssetService, EventService) -> TiledService = { assetService, eventService ->
        TiledService(assetService, eventService)
    },
    shaderServiceInitializer: () -> ShaderService = { ShaderService() },
) : ServiceLocator {

    override val batch: Batch by lazy(batchInitializer)
    override val asset: AssetService by lazy(assetServiceInitializer)
    override val event: EventService by lazy(eventServiceInitializer)
    override val tiled: TiledService by lazy { tiledServiceInitializer(asset, event) }
    override val shader: ShaderService by lazy { shaderServiceInitializer() }

    override fun dispose() {
        batch.disposeSafely()
        asset.disposeSafely()
        shader.disposeSafely()
    }

}
