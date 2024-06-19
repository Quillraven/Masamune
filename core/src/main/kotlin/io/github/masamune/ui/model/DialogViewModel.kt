package io.github.masamune.ui.model

import com.github.quillraven.fleks.Entity
import io.github.masamune.dialog.Dialog
import io.github.masamune.event.*
import io.github.masamune.ui.model.DialogUiContent.Companion.EMPTY_CONTENT

data class DialogUiContent(
    var txt: String = "",
    var options: List<String> = mutableListOf(),
    var image: String? = "",
    var caption: String? = null,
) {
    companion object {
        val EMPTY_CONTENT = DialogUiContent()
    }
}

class DialogViewModel(private val eventService: EventService) : PropertyChangeSource(), EventListener {

    private lateinit var activeDialog: Dialog
    var content: DialogUiContent by propertyNotify(EMPTY_CONTENT)
    var selectedOptionIdx: Int by propertyNotify(-1)

    private var player: Entity = Entity.NONE
    private var other: Entity = Entity.NONE

    private fun selectOption(idx: Int) {
        val realIdx = when {
            idx < 0 -> activeDialog.activePage.options.size - 1
            idx >= activeDialog.activePage.options.size -> 0
            else -> idx
        }

        selectedOptionIdx = realIdx
    }

    override fun onEvent(event: Event) {
        when (event) {
            is DialogBeginEvent -> {
                activeDialog = event.dialog
                player = event.player
                other = event.other

                val activePage = activeDialog.activePage
                content = DialogUiContent(
                    txt = activePage.text,
                    options = activePage.options.map { it.text },
                    image = activePage.image,
                    caption = activePage.imageCaption
                )
                selectedOptionIdx = 0
            }

            is UiSelectEvent -> {
                if (activeDialog.triggerOption(selectedOptionIdx)) {
                    content = EMPTY_CONTENT
                    eventService.fire(DialogEndEvent(player, other, activeDialog, selectedOptionIdx))
                }
            }

            is UiUpEvent -> selectOption(selectedOptionIdx + 1)
            is UiDownEvent -> selectOption(selectedOptionIdx - 1)

            else -> Unit
        }
    }

}
