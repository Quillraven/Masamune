package io.github.masamune.screen

import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import io.github.masamune.asset.ShaderService
import ktx.app.KtxScreen

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
    private val speed = 1f / type.time
    private var currentBlur = startBlur
    private var alpha: Float = 0f

    override fun isDone(): Boolean = alpha >= 1f

    override fun render(delta: Float) {
        alpha = (alpha + delta * speed).coerceAtMost(1f)
        currentBlur = MathUtils.lerp(startBlur, endBlur, alpha)

        shaderService.useBlurShader(batch, radius = currentBlur) {
            screen.render(0f)
        }
    }
}
