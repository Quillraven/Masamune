package io.github.masamune.system

import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.masamune.component.Tag
import io.github.masamune.component.Transform
import io.github.masamune.event.Event
import io.github.masamune.event.EventListener
import io.github.masamune.event.MapChangeEvent
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.width
import kotlin.math.max

class CameraSystem(
    gameViewport: Viewport = inject(),
) : IteratingSystem(family { all(Transform, Tag.CAMERA_FOCUS) }), EventListener {

    private val camera = gameViewport.camera
    private val mapBoundaries = vec2(0f, 0f)

    override fun onTickEntity(entity: Entity) {
        val (position, size) = entity[Transform]
        val halfW = size.x * 0.5f
        val halfH = size.y * 0.5f

        var newCamX = position.x + halfW
        var newCamY = position.y + halfH
        if (!mapBoundaries.isZero) {
            val viewportW = camera.viewportWidth * 0.5f
            val viewportH = camera.viewportHeight * 0.5f

            newCamX = newCamX.coerceIn(viewportW, max(viewportW, mapBoundaries.x - viewportW))
            newCamY = newCamY.coerceIn(viewportH, max(viewportH, mapBoundaries.y - viewportH))
        }

        camera.position.set(newCamX, newCamY, 0f)
        camera.update()
    }

    override fun onEvent(event: Event) {
        when (event) {
            is MapChangeEvent -> {
                mapBoundaries.set(event.tiledMap.width.toFloat(), event.tiledMap.height.toFloat())
            }

            else -> Unit
        }
    }

}
