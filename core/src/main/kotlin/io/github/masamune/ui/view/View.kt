package io.github.masamune.ui.view

import io.github.masamune.event.*

interface View : EventListener {

    fun onUpPressed() = Unit
    fun onDownPressed() = Unit
    fun onSelectPressed() = Unit

    override fun onEvent(event: Event) = when (event) {
        is UiUpEvent -> onUpPressed()
        is UiDownEvent -> onDownPressed()
        is UiSelectEvent -> onSelectPressed()
        else -> Unit
    }
}
