package io.github.masamune.ui.model

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.World
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.ActionExecutorService.Companion.LIFE_PER_CONST
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
                val playerEquipment = player[Equipment]
                equipmentBonus = UIStats.entries.associateWith { uiStat ->
                    playerEquipment.items
                        .map { it[Stats] }
                        .sumOf {
                            when (uiStat) {
                                UIStats.AGILITY -> it.agility.toInt()
                                UIStats.ARCANE_STRIKE -> (it.arcaneStrike * 100).toInt()
                                UIStats.ARMOR -> it.armor.toInt()
                                UIStats.CONSTITUTION -> it.constitution.toInt()
                                UIStats.CRITICAL_STRIKE -> (it.criticalStrike * 100).toInt()
                                UIStats.DAMAGE -> it.damage.toInt()
                                UIStats.INTELLIGENCE -> it.intelligence.toInt()
                                UIStats.LIFE_MAX -> it.lifeMax.toInt()
                                UIStats.MAGICAL_EVADE -> (it.magicalEvade * 100).toInt()
                                UIStats.MANA_MAX -> it.manaMax.toInt()
                                UIStats.PHYSICAL_EVADE -> (it.physicalEvade * 100).toInt()
                                UIStats.RESISTANCE -> it.resistance.toInt()
                                UIStats.STRENGTH -> it.strength.toInt()
                                else -> 0
                            }
                        }
                }

                // trigger combat view updates
                playerName = player[Name].name
                val statsCmp = player[Stats]
                val defaultStats = uiMapOf(statsCmp, player[Experience], player[Inventory])
                defaultStats[UIStats.LIFE_MAX] = "${(statsCmp.totalLifeMax + equipmentBonus(UIStats.LIFE_MAX) + equipmentBonus(UIStats.CONSTITUTION) * LIFE_PER_CONST).toInt()}"
                defaultStats[UIStats.MANA_MAX] = "${(statsCmp.totalManaMax + equipmentBonus(UIStats.MANA_MAX)).toInt()}"
                playerStats = defaultStats
            }
        }
    }

}
