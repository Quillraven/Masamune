package io.github.masamune.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntityBy
import io.github.masamune.Masamune.Companion.UNIT_SCALE
import io.github.masamune.component.Graphic
import io.github.masamune.component.Transform
import io.github.masamune.event.Event
import io.github.masamune.event.EventListener
import io.github.masamune.event.MapChangeEvent
import ktx.assets.disposeSafely
import ktx.tiled.use

class RenderSystem(
    private val batch: Batch = inject(),
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(
    family = family { all(Transform, Graphic) },
    comparator = compareEntityBy(Transform)
), EventListener {

    private val camera: OrthographicCamera = gameViewport.camera as OrthographicCamera
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, batch)
    private val background = mutableListOf<TiledMapTileLayer>()
    private val foreground = mutableListOf<TiledMapTileLayer>()

    override fun onTick() {
        gameViewport.apply()
        mapRenderer.use(camera) { renderer ->
            background.forEach { renderer.renderTileLayer(it) }
            super.onTick()
            foreground.forEach { renderer.renderTileLayer(it) }
        }
    }

    /**
     * Set entity's sprite position, scaling and rotation to its transform values
     * and draw the sprite.
     */
    override fun onTickEntity(entity: Entity) {
        val (texRegion, regionSize, color) = entity[Graphic]
        val (position, size, scale, rotationDeg) = entity[Transform]

        // fill texture inside transform size by keeping aspect ratio
        val realSize = Scaling.fill.apply(regionSize.x, regionSize.y, size.x, size.y)
        batch.color = color
        batch.draw(
            texRegion,
            position.x, position.y,
            realSize.x * 0.5f, realSize.y * 0.5f,
            realSize.x, realSize.y,
            scale, scale,
            rotationDeg
        )
    }

    override fun onEvent(event: Event) {
        when (event) {
            is MapChangeEvent -> {
                background.clear()
                foreground.clear()
                var activeLayers = background
                event.tiledMap.layers.forEach { layer ->
                    if (layer::class == MapLayer::class) {
                        activeLayers = foreground
                    }
                    if (layer !is TiledMapTileLayer) {
                        return@forEach
                    }

                    activeLayers += layer
                }
            }
        }
    }

    override fun onDispose() {
        mapRenderer.disposeSafely()
    }

}
