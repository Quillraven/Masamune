package io.github.masamune.screen

import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import io.github.masamune.asset.ShaderService
import ktx.app.KtxScreen
import ktx.graphics.component1
import ktx.graphics.component2
import ktx.graphics.component3
import ktx.graphics.component4

sealed class Transition(val screen: Screen) {
    abstract fun render(delta: Float)

    abstract fun isDone(): Boolean

    fun isNotDone(): Boolean = !isDone()
}

data object DefaultTransition : Transition(object : KtxScreen {}) {
    override fun isDone(): Boolean = true

    override fun render(delta: Float) = Unit
}

class BlurTransition(
    type: BlurTransitionType,
    screen: Screen,
    private val batch: Batch,
    private val shaderService: ShaderService,
) : Transition(screen) {

    private val startBlur: Float = type.startBlur
    private val endBlur: Float = type.endBlur
    private val startAlpha: Float = type.startAlpha
    private val endAlpha: Float = type.endAlpha
    private val interpolation: Interpolation = type.interpolation
    private val speed = 1f / type.time
    private var currentBlur = startBlur
    private var currentAlpha = startAlpha
    private var alpha: Float = 0f

    override fun isDone(): Boolean = alpha >= 1f

    override fun render(delta: Float) {
        alpha = (alpha + delta * speed).coerceAtMost(1f)
        currentBlur = interpolation.apply(startBlur, endBlur, alpha)
        currentAlpha = interpolation.apply(startAlpha, endAlpha, alpha)

        shaderService.useBlurShader(batch, radius = currentBlur) {
            val (r, g, b, a) = batch.color
            batch.setColor(r, g, b, currentAlpha)
            screen.render(0f)
            batch.setColor(r, g, b, a)
        }
    }
}
