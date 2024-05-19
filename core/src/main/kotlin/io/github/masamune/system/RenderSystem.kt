package io.github.masamune.system

import com.badlogic.gdx.graphics.g2d.Batch
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
        val (sprite) = entity[Graphic]
        val (position, size, scale, rotationDeg) = entity[Transform]

        with(sprite) {
            // fill sprite inside transform size by keeping aspect ratio
            val spriteSize = Scaling.fill.apply(regionWidth * UNIT_SCALE, regionHeight * UNIT_SCALE, size.x, size.y)
            setOrigin(spriteSize.x * 0.5f, spriteSize.y * 0.5f)
            rotation = rotationDeg
            setScale(scale)
            setBounds(position.x, position.y, spriteSize.x , spriteSize.y )
            draw(batch)
        }
    }

}
