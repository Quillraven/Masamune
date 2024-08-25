package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import io.github.masamune.event.Event
import io.github.masamune.event.EventListener
import io.github.masamune.event.UiBackEvent
import io.github.masamune.event.UiDownEvent
import io.github.masamune.event.UiSelectEvent
import io.github.masamune.event.UiUpEvent

abstract class View(skin: Skin) : Table(skin), EventListener {

    open fun onUpPressed() = Unit
    open fun onDownPressed() = Unit
    open fun onSelectPressed() = Unit
    open fun onBackPressed() = Unit

    override fun onEvent(event: Event) {
        if (!isVisible) {
            return
        }

        when (event) {
            is UiUpEvent -> onUpPressed()
            is UiDownEvent -> onDownPressed()
            is UiSelectEvent -> onSelectPressed()
            is UiBackEvent -> onBackPressed()
            else -> Unit
        }
    }
}
