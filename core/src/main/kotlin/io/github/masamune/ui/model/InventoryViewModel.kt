package io.github.masamune.ui.model

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.World
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.ActionExecutorService.Companion.LIFE_PER_CONST
import io.github.masamune.component.Equipment
import io.github.masamune.component.Inventory
import io.github.masamune.component.Item
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.equipItem
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.event.MenuBeginEvent
import io.github.masamune.event.MenuEndEvent
import io.github.masamune.removeEquipment
import io.github.masamune.removeItem
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.tiledmap.ItemType

class InventoryViewModel(
    bundle: I18NBundle,
    audioService: AudioService,
    private val world: World,
    private val eventService: EventService,
) : ViewModel(bundle, audioService) {

    private val playerEntities = world.family { all(Player) }
    private lateinit var equipmentBonus: Map<UIStats, Int>
    private val emptyItems = listOf(
        emptyItemModel(ItemCategory.WEAPON, "<< ${i18nTxt(I18NKey.MENU_OPTION_CLEAR_ITEM)} >>"),
        emptyItemModel(ItemCategory.ARMOR, "<< ${i18nTxt(I18NKey.MENU_OPTION_CLEAR_ITEM)} >>"),
        emptyItemModel(ItemCategory.HELMET, "<< ${i18nTxt(I18NKey.MENU_OPTION_CLEAR_ITEM)} >>"),
        emptyItemModel(ItemCategory.BOOTS, "<< ${i18nTxt(I18NKey.MENU_OPTION_CLEAR_ITEM)} >>"),
        emptyItemModel(ItemCategory.ACCESSORY, "<< ${i18nTxt(I18NKey.MENU_OPTION_CLEAR_ITEM)} >>"),
    )

    // view properties
    var playerName by propertyNotify("")
    var playerStats: Map<UIStats, String> by propertyNotify(emptyMap())
    var playerEquipment: Map<ItemCategory, ItemModel> by propertyNotify(emptyMap())
    var equipmentItems: List<ItemModel> by propertyNotify(emptyList())
    var inventoryItems: List<ItemModel> by propertyNotify(emptyList())

    override fun onEvent(event: Event) {
        if (event !is MenuBeginEvent || event.type != MenuType.INVENTORY) {
            return
        }

        with(world) {
            val player = playerEntities.first()
            playerName = player[Name].name

            // calculate equipment bonus before setting player stats because StatsView reacts to playerStats change
            val equipmentCmp = player[Equipment]
            updatePlayerEquipment(equipmentCmp)
            val itemPartition = player[Inventory].items.partition { it[Item].category.isEquipment }
            inventoryItems = itemPartition.second.map { it.toItemModel(world) }
            val playerEquipmentItems = itemPartition.first.map { it.toItemModel(world) }
            equipmentItems = emptyItems + playerEquipmentItems

            updatePlayerStats(player[Stats], equipmentCmp)
        }
    }

    private fun updatePlayerStats(statsCmp: Stats, equipmentCmp: Equipment) {
        val equipStats = equipmentCmp.run { world.toStats() }
        val finalStats = Stats.of(statsCmp).apply {
            strength += equipStats.strength
            agility += equipStats.agility
            intelligence += equipStats.intelligence
            damage += equipStats.damage
            armor += equipStats.armor
            resistance += equipStats.resistance

            val baseLife = lifeMax + constitution * LIFE_PER_CONST
            lifeMax = baseLife + equipStats.lifeMax + equipStats.constitution * LIFE_PER_CONST
            manaMax += equipStats.manaMax

            // update constituion AFTER life max was calculated to not include equipment bonus twice
            constitution += equipStats.constitution
        }
        val defaultStats = uiMapOf(finalStats)
        playerStats = defaultStats
    }

    private fun updatePlayerEquipment(equipmentCmp: Equipment) {
        playerEquipment = equipmentCmp.items.map {
            val itemModel = it.toItemModel(world)
            itemModel.category to itemModel
        }.toMap()
        equipmentBonus = equipmentCmp.toUiStatsMap(world)
    }

    fun quit() {
        eventService.fire(MenuEndEvent)
        eventService.fire(MenuBeginEvent(MenuType.GAME))
        // setting player name to blank will hide the inventory view again
        playerName = ""
    }

    fun calcDiff(itemModel: ItemModel): Map<UIStats, Int> {
        return playerEntities.first().calcEquipmentDiff(itemModel, world)
    }

    fun equip(category: ItemCategory, itemIdx: Int) = with(world) {
        val playerEntity = playerEntities.first()
        val selectedItemModel = equipmentItems.filter { it.category == category }[itemIdx]
        val inventoryCmp = playerEntity[Inventory]
        if (selectedItemModel.type == ItemType.UNDEFINED) {
            // special unequip item
            world.removeEquipment(selectedItemModel.category, playerEntity)
        } else {
            // equip item (move from inventory to equipment component)
            val itemEntity = inventoryCmp.items.single { it[Item].type == selectedItemModel.type }
            world.equipItem(itemEntity, playerEntity)
            // do NOT remove the item entity because it still exists. It just got moved to the equipment component items
            world.removeItem(selectedItemModel.type, 1, playerEntity, removeEntity = false)
        }

        // update equipment ItemModel
        val equipmentCmp = playerEntity[Equipment]
        updatePlayerEquipment(equipmentCmp)
        updatePlayerStats(playerEntity[Stats], equipmentCmp)
        equipmentItems = emptyItems + inventoryCmp.items
            .filter { it[Item].category.isEquipment }
            .map { it.toItemModel(world) }
    }

}
