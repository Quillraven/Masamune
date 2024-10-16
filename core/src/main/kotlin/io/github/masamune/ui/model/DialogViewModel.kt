package io.github.masamune.ui.model

import com.github.quillraven.fleks.Entity
import io.github.masamune.dialog.Dialog
import io.github.masamune.event.DialogBeginEvent
import io.github.masamune.event.DialogEndEvent
import io.github.masamune.event.DialogOptionChange
import io.github.masamune.event.DialogOptionTrigger
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
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

class DialogViewModel(private val eventService: EventService) : ViewModel() {

    private lateinit var activeDialog: Dialog
    var content: DialogUiContent by propertyNotify(EMPTY_CONTENT)

    private var player: Entity = Entity.NONE

    fun triggerOption(optionIdx: Int) {
        eventService.fire(DialogOptionTrigger)
        if (activeDialog.triggerOption(optionIdx)) {
            // dialog finished
            content = EMPTY_CONTENT
            eventService.fire(DialogEndEvent(player, activeDialog, optionIdx))
            return
        }

        // go to next page
        updateContent()
    }

    fun optionChanged() {
        eventService.fire(DialogOptionChange)
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
