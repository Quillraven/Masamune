package io.github.masamune.screen

import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.Batch
import io.github.masamune.asset.ShaderService
import ktx.log.logger

class ScreenTransitionService(
    val batch: Batch,
    val shaderService: ShaderService,
) {
    private var fromTransition: Transition = DefaultTransition
    private var toTransition: Transition = DefaultTransition
    private var transitionEnd: () -> Unit = {}

    val hasActiveTransition: Boolean
        get() {
            val fromDone = fromTransition.isDone()
            val toDone = toTransition.isDone()
            return !fromDone || !toDone
        }

    private fun transition(screen: Screen, type: TransitionType): Transition {
        return when (type) {
            is BlurTransitionType -> BlurTransition(type, screen, batch, shaderService)
        }
    }

    fun transition(
        fromScreen: Screen,
        fromType: TransitionType,
        toScreen: Screen,
        toType: TransitionType,
        onTransitionEnd: () -> Unit
    ) {
        fromTransition = transition(fromScreen, fromType)
        toTransition = transition(toScreen, toType)
        transitionEnd = onTransitionEnd
    }

    fun render(delta: Float) {
        if (fromTransition.isNotDone()) {
            fromTransition.render(delta)
        }
        if (toTransition.isNotDone()) {
            toTransition.render(delta)
        }

        if (fromTransition.isDone() && toTransition.isDone()) {
            // transition finished -> set new screen
            log.debug { "Screen transition finished" }
            fromTransition = DefaultTransition
            toTransition = DefaultTransition
            transitionEnd()
        }
    }

    companion object {
        private val log = logger<ScreenTransitionService>()
    }
}
