package io.github.masamune.system

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
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
import io.github.masamune.event.MapTransitionBeginEvent
import io.github.masamune.tiledmap.MapTransitionType
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.width
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class CameraSystem(
    gameViewport: Viewport = inject(),
) : IteratingSystem(family { all(Transform, Tag.CAMERA_FOCUS) }), EventListener {

    private val camera = gameViewport.camera
    private val mapBoundaries = vec2(0f, 0f)

    // pan properties
    private val panFrom = vec2()
    private val panTo = vec2()
    private var panAlpha = 0f
    private var panSpeed = 0f
    private var panInterpolation = Interpolation.linear

    override fun onTick() {
        if (panAlpha > 0f) {
            // active camera pan -> ignore entity target lock
            panAlpha = (panAlpha - deltaTime * panSpeed).coerceAtLeast(0f)
            val alpha = 1f - panAlpha
            val panX = panInterpolation.apply(panFrom.x, panTo.x, alpha)
            val panY = panInterpolation.apply(panFrom.y, panTo.y, alpha)
            camera.position.set(panX, panY, camera.position.z)
            camera.update()
            return
        }

        // lock camera on entity with CAMERA_FOCUS tag
        super.onTick()
    }

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
        camera.position.set(newCamX, newCamY, camera.position.z)
        camera.update()
    }

    private fun panTo(to: Vector2, time: Float, interpolation: Interpolation) {
        panFrom.set(camera.position.x, camera.position.y)
        panTo.set(to)
        panAlpha = 1f
        panSpeed = 1f / time
        panInterpolation = interpolation
    }

    override fun onEvent(event: Event) {
        when (event) {
            is MapChangeEvent -> {
                mapBoundaries.set(event.tiledMap.width.toFloat(), event.tiledMap.height.toFloat())
            }

            is MapTransitionBeginEvent -> {
                val (fromMap, toMap, time, interpolation, type, offset) = event
                val halfCamW = camera.viewportWidth * 0.5f
                val halfCamH = camera.viewportHeight * 0.5f

                val cameraTargetLoc = when (type) {
                    MapTransitionType.TOP_TO_BOTTOM -> {
                        val shownTilesX = fromMap.width - abs(offset.x)
                        val maxTilesX = min(fromMap.width.toFloat(), camera.viewportWidth)
                        val panOffsetX = maxTilesX - shownTilesX
                        var sign = sign(fromMap.width.toFloat() - toMap.width.toFloat())
                        sign = if (sign == 0f) 1f else sign
                        vec2(camera.position.x + panOffsetX * sign, fromMap.height + halfCamH)
                    }

                    MapTransitionType.BOTTOM_TO_TOP -> {
                        val shownTilesX = toMap.width - abs(offset.x)
                        val maxTilesX = min(toMap.width.toFloat(), camera.viewportWidth)
                        val panOffsetX = maxTilesX - shownTilesX
                        val panOffsetY = min(toMap.height.toFloat(), camera.viewportHeight)
                        var sign = sign(toMap.width.toFloat() - fromMap.width.toFloat())
                        sign = if (sign == 0f) 1f else sign
                        vec2(camera.position.x - panOffsetX * sign, camera.position.y - panOffsetY)
                    }

                    MapTransitionType.LEFT_TO_RIGHT -> {
                        val panOffsetX = min(toMap.width.toFloat(), camera.viewportWidth)
                        val shownTilesY = toMap.height - abs(offset.y)
                        val maxTilesY = min(toMap.height.toFloat(), camera.viewportHeight)
                        val panOffsetY = maxTilesY - shownTilesY
                        var sign = sign(toMap.height.toFloat() - fromMap.height.toFloat())
                        sign = if (sign == 0f) 1f else sign
                        vec2(camera.position.x - panOffsetX, camera.position.y - panOffsetY * sign)
                    }

                    MapTransitionType.RIGHT_TO_LEFT -> {
                        val shownTilesY = fromMap.height - abs(offset.y)
                        val maxTilesY = min(fromMap.height.toFloat(), camera.viewportHeight)
                        val panOffsetY = maxTilesY - shownTilesY
                        var sign = sign(fromMap.height.toFloat() - toMap.height.toFloat())
                        sign = if (sign == 0f) 1f else sign
                        vec2(fromMap.width + halfCamW, camera.position.y + panOffsetY * sign)
                    }
                }

                panTo(cameraTargetLoc, time, interpolation)
            }

            else -> Unit
        }
    }

}
