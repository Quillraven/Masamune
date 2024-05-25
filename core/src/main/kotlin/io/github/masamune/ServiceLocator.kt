package io.github.masamune

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import io.github.masamune.asset.AssetService
import io.github.masamune.event.EventService
import io.github.masamune.tiledmap.TiledService
import ktx.assets.disposeSafely

class ServiceLocator(
    val batch: Batch = SpriteBatch(),
    val asset: AssetService = AssetService(),
    val event: EventService = EventService(),
    val tiled: TiledService = TiledService(asset, event),
) : Disposable {

    override fun dispose() {
        batch.disposeSafely()
        asset.disposeSafely()
    }

}
