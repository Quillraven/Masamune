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

    private var player: Entity = Entity.NONE

    fun triggerOption(optionIdx: Int) {
        if (activeDialog.triggerOption(optionIdx)) {
            // dialog finished
            content = EMPTY_CONTENT
            eventService.fire(DialogEndEvent(player, activeDialog, optionIdx))
            return
        }

        // go to next page
        updateContent()
    }

    private fun updateContent() {
        val activePage = activeDialog.activePage
        content = DialogUiContent(
            txt = activePage.text,
            options = activePage.options.map { it.text },
            image = activePage.image,
            caption = activePage.imageCaption
        )
    }

    override fun onEvent(event: Event) {
        when (event) {
            is DialogBeginEvent -> {
                activeDialog = event.dialog
                player = event.player

                updateContent()
            }

            else -> Unit
        }
    }

}
