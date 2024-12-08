package io.github.masamune.screen

sealed interface TransitionType

data class BlurTransitionType(
    val startBlur: Float,
    val endBlur: Float,
    val time: Float,
) : TransitionType
