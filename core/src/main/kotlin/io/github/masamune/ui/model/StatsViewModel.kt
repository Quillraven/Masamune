package io.github.masamune.ui.model

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.World
import io.github.masamune.component.Experience
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.event.MenuBeginEvent
import io.github.masamune.event.MenuEndEvent

class StatsViewModel(
    private val bundle: I18NBundle,
    private val world: World,
    private val eventService: EventService,
) : ViewModel() {

    private val playerEntities = world.family { all(Player) }

    var playerName: String by propertyNotify("")

    // pair-left = localized text for UIStat, pair-right = value
    var playerStats: Map<UIStats, Pair<String, String>> by propertyNotify(emptyMap())

    fun triggerClose() {
        eventService.fire(MenuEndEvent)
        playerName = ""
        eventService.fire(MenuBeginEvent(MenuType.GAME))
    }

    override fun onEvent(event: Event) {
        if (event is MenuBeginEvent && event.type == MenuType.STATS) {
            with(world) {
                val player = playerEntities.first()
                playerName = player[Name].name

                val experience = player[Experience]
                val stats = player[Stats].tiledStats
                playerStats = mapOf(
                    UIStats.AGILITY to (bundle["stats.agility"] to "${stats.agility.toInt()}"),
                    UIStats.ARCANE_STRIKE to (bundle["stats.arcaneStrike"] to "${(stats.arcaneStrike / 100).toInt()}%"),
                    UIStats.ARMOR to (bundle["stats.armor"] to "${stats.armor.toInt()}"),
                    UIStats.CONSTITUTION to (bundle["stats.constitution"] to "${stats.constitution.toInt()}"),
                    UIStats.CRITICAL_STRIKE to (bundle["stats.criticalStrike"] to "${(stats.criticalStrike / 100).toInt()}%"),
                    UIStats.ATTACK to (bundle["stats.attack"] to "${stats.damage.toInt()}"),
                    UIStats.INTELLIGENCE to (bundle["stats.intelligence"] to "${stats.intelligence.toInt()}"),
                    UIStats.LIFE to (bundle["stats.life"] to "${stats.life.toInt()}"),
                    UIStats.LIFE_MAX to (bundle["stats.life"] to "${stats.lifeMax.toInt()}"),
                    UIStats.MAGICAL_EVADE to (bundle["stats.magicalEvade"] to "${(stats.magicalEvade / 100).toInt()}%"),
                    UIStats.MANA to (bundle["stats.mana"] to "${stats.mana.toInt()}"),
                    UIStats.MANA_MAX to (bundle["stats.mana"] to "${stats.manaMax.toInt()}"),
                    UIStats.PHYSICAL_EVADE to (bundle["stats.physicalEvade"] to "${(stats.physicalEvade / 100).toInt()}%"),
                    UIStats.RESISTANCE to (bundle["stats.resistance"] to "${stats.resistance.toInt()}"),
                    UIStats.STRENGTH to (bundle["stats.strength"] to "${stats.strength.toInt()}"),
                    // experience
                    UIStats.LEVEL to (bundle["stats.level"] to "${experience.level}"),
                    UIStats.XP to (bundle["stats.xp"] to "${experience.current}"),
                    UIStats.XP_NEEDED to (bundle["stats.xpNeeded"] to "${experience.forLevelUp}"),
                )
            }
        }
    }

}
