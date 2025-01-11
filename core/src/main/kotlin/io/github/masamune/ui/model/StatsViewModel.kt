package io.github.masamune.ui.model

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.World
import io.github.masamune.audio.AudioService
import io.github.masamune.component.Equipment
import io.github.masamune.component.Experience
import io.github.masamune.component.Inventory
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.event.MenuBeginEvent
import io.github.masamune.event.MenuEndEvent

class StatsViewModel(
    bundle: I18NBundle,
    audioService: AudioService,
    private val world: World,
    private val eventService: EventService,
) : ViewModel(bundle, audioService) {

    private val playerEntities = world.family { all(Player) }
    private var equipmentBonus: Map<UIStats, Int> = emptyMap()

    // pair-left = localized text for UIStat, pair-right = value
    var playerStats: Map<UIStats, String> by propertyNotify(emptyMap())
    var playerName: String by propertyNotify("")

    fun triggerClose() {
        eventService.fire(MenuEndEvent)
        playerName = ""
        eventService.fire(MenuBeginEvent(MenuType.GAME))
    }

    fun equipmentBonus(uiStat: UIStats): Int = equipmentBonus.getOrDefault(uiStat, 0)

    override fun onEvent(event: Event) {
        if (event is MenuBeginEvent && event.type == MenuType.STATS) {
            with(world) {
                val player = playerEntities.first()

                // calculate equipment bonus before setting player stats because StatsView reacts to playerStats change
                equipmentBonus = player[Equipment].toUiStatsMap(world)

                // trigger combat view updates
                playerName = player[Name].name
                val statsCmp = player[Stats]
                val defaultStats = uiMapOf(statsCmp, player[Experience], player[Inventory]).andEquipmentBonus(equipmentBonus)
                playerStats = defaultStats
            }
        }
    }

}
