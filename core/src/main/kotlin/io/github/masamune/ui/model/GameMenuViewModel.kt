package io.github.masamune.ui.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.event.GameExitEvent
import io.github.masamune.event.MenuBeginEvent
import io.github.masamune.event.MenuEndEvent

class GameMenuViewModel(
    private val bundle: I18NBundle,
    private val eventService: EventService
) : ViewModel() {

    var options: List<String> by propertyNotify(listOf())

    fun triggerOption(optionIdx: Int) {
        when (optionIdx) {
            // quit
            options.lastIndex -> {
                eventService.fire(GameExitEvent)
                Gdx.app.exit()
            }
            // stats
            0 -> {
                triggerClose()
                eventService.fire(MenuBeginEvent(MenuType.STATS))
            }
        }
    }

    fun triggerClose() {
        options = emptyList()
        eventService.fire(MenuEndEvent)
    }

    override fun onEvent(event: Event) {
        if (event is MenuBeginEvent && event.type == MenuType.GAME) {
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
