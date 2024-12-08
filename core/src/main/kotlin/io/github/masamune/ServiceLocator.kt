package io.github.masamune

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.ShaderService
import io.github.masamune.audio.AudioService
import io.github.masamune.event.EventService
import io.github.masamune.screen.ScreenTransitionService
import io.github.masamune.tiledmap.ImmediateMapTransitionService
import io.github.masamune.tiledmap.MapTransitionService
import io.github.masamune.tiledmap.TiledService

interface ServiceLocator : Disposable {
    val batch: Batch
    val asset: AssetService
    val event: EventService
    val tiled: TiledService
    val shader: ShaderService
    val mapTransition: MapTransitionService
    val audio: AudioService
    val screenTransition: ScreenTransitionService
}

class LazyServiceLocator(
    batchInitializer: () -> Batch = { SpriteBatch() },
    assetServiceInitializer: () -> AssetService = { AssetService() },
    eventServiceInitializer: () -> EventService = { EventService() },
    tiledServiceInitializer: (AssetService, EventService) -> TiledService = { assetService, eventService ->
        TiledService(assetService, eventService)
    },
    shaderServiceInitializer: () -> ShaderService = { ShaderService() },
    mapTransitionServiceInitializer: (TiledService) -> MapTransitionService = { tiledService ->
        ImmediateMapTransitionService(tiledService)
    },
    audioServiceInitializer: (AssetService) -> AudioService = { assetService -> AudioService(assetService) },
    screenTransitionServiceInitializer: (Batch, ShaderService) -> ScreenTransitionService = { batch, shaderService ->
        ScreenTransitionService(batch, shaderService)
    },
) : ServiceLocator {

    override val batch: Batch by lazy(batchInitializer)
    override val asset: AssetService by lazy(assetServiceInitializer)
    override val event: EventService by lazy(eventServiceInitializer)
    override val tiled: TiledService by lazy { tiledServiceInitializer(asset, event) }
    override val shader: ShaderService by lazy { shaderServiceInitializer() }
    override val mapTransition: MapTransitionService by lazy { mapTransitionServiceInitializer(tiled) }
    override val audio: AudioService by lazy { audioServiceInitializer(asset) }
    override val screenTransition: ScreenTransitionService by lazy { screenTransitionServiceInitializer(batch, shader) }

    override fun dispose() {
        batch.dispose()
        asset.dispose()
        shader.dispose()
    }

}
