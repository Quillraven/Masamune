package io.github.masamune.ui.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import io.github.masamune.audio.AudioService
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.event.GameExitEvent
import io.github.masamune.event.MenuBeginEvent
import io.github.masamune.event.MenuEndEvent

class GameMenuViewModel(
    bundle: I18NBundle,
    audioService: AudioService,
    private val eventService: EventService
) : ViewModel(bundle, audioService) {

    var options: List<String> by propertyNotify(listOf())

    fun triggerOption(optionIdx: Int) {
        playSndMenuAccept()
        when (optionIdx) {
            // quit
            options.lastIndex -> {
                eventService.fire(GameExitEvent)
                Gdx.app.exit()
            }
            // stats
            0 -> {
                // do not fire option event to avoid overlapping sound effect of two OptionTrigger events
                triggerClose(fireOptionEvent = false)
                eventService.fire(MenuBeginEvent(MenuType.STATS))
            }
        }
    }

    fun triggerClose(fireOptionEvent: Boolean = true) {
        if (fireOptionEvent) {
            playSndMenuAccept()
        }
        options = emptyList()
        eventService.fire(MenuEndEvent)
    }

    override fun onEvent(event: Event) {
        if (event is MenuBeginEvent && event.type == MenuType.GAME) {
            playSndMenuAccept()
            options = listOf(
                bundle["menu.option.stats"],
                bundle["menu.option.inventory"],
                bundle["menu.option.quests"],
                bundle["menu.option.save"],
                bundle["menu.option.quit"],
            )
        }
    }
}
