package io.github.masamune.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.masamune.component.Graphic
import io.github.masamune.component.Transform
import ktx.assets.disposeSafely
import ktx.graphics.use

class DebugRenderSystem(
    private val shapeRenderer: ShapeRenderer = ShapeRenderer(),
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(family { all(Transform, Graphic) }) {

    override fun onTick() {
        gameViewport.apply()
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, gameViewport.camera) {
            shapeRenderer.color = Color.RED
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val (position, size, scale, rotation) = entity[Transform]
        shapeRenderer.rect(
            position.x, position.y,
            size.x * scale * 0.5f, size.y * scale * 0.5f,
            size.x * scale, size.y * scale,
            1f, 1f,
            rotation
        )
    }

    override fun onDispose() {
        shapeRenderer.disposeSafely()
    }

}
