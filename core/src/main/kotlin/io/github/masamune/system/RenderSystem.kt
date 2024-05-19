package io.github.masamune.system

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntityBy
import io.github.masamune.component.Graphic
import io.github.masamune.component.Transform
import ktx.graphics.use

class RenderSystem(
    private val batch: Batch = inject(),
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(
    family = family { all(Transform, Graphic) },
    comparator = compareEntityBy(Transform)
) {

    override fun onTick() {
        gameViewport.apply()
        batch.use(gameViewport.camera) {
            super.onTick()
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

}
