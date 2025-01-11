package io.github.masamune.ui.model

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.World
import io.github.masamune.audio.AudioService
import io.github.masamune.component.Equipment
import io.github.masamune.component.Inventory
import io.github.masamune.component.Item
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.event.MenuBeginEvent
import io.github.masamune.event.MenuEndEvent
import io.github.masamune.tiledmap.ItemCategory

class InventoryViewModel(
    bundle: I18NBundle,
    audioService: AudioService,
    private val world: World,
    private val eventService: EventService,
) : ViewModel(bundle, audioService) {

    private val playerEntities = world.family { all(Player) }
    private lateinit var equipmentBonus: Map<UIStats, Int>
    private lateinit var inventoryItems: List<ItemModel>
    private lateinit var equipmentItems: List<ItemModel>
    private lateinit var playerEquipment: Map<ItemCategory, ItemModel>

    // view properties
    var playerName by propertyNotify("")
    var playerStats: Map<UIStats, String> by propertyNotify(emptyMap())

    override fun onEvent(event: Event) {
        if (event !is MenuBeginEvent || event.type != MenuType.INVENTORY) {
            return
        }

        with(world) {
            val player = playerEntities.first()
            playerName = player[Name].name

            // calculate equipment bonus before setting player stats because StatsView reacts to playerStats change
            val equipmentCmp = player[Equipment]
            equipmentBonus = equipmentCmp.toUiStatsMap(world)
            playerEquipment = equipmentCmp.items.map {
                val itemModel = it.toItemModel(world)
                itemModel.category to itemModel
            }.toMap()
            val itemPartition = player[Inventory].items.partition { it[Item].category.isEquipment }
            inventoryItems = itemPartition.second.map { it.toItemModel(world) }
            equipmentItems = itemPartition.first.map { it.toItemModel(world) }

            val statsCmp = player[Stats]
            val defaultStats = uiMapOf(statsCmp).andEquipmentBonus(equipmentBonus)
            playerStats = defaultStats
        }
    }

    fun quit() {
        eventService.fire(MenuEndEvent)
        eventService.fire(MenuBeginEvent(MenuType.GAME))
        playSndMenuAccept()
    }

    fun items(): List<ItemModel> = inventoryItems

    fun item(idx: Int): ItemModel = inventoryItems[idx]

    fun playerEquipment(): Map<ItemCategory, ItemModel> = playerEquipment

    fun inventoryEquipment(): List<ItemModel> = equipmentItems

    fun inventoryEquipment(idx: Int): ItemModel = equipmentItems[idx]

    fun calcDiff(itemIdx: Int): Map<UIStats, Int> {
        return playerEntities.first().calcEquipmentDiff(inventoryEquipment(itemIdx), world)
    }

}
