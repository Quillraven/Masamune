package io.github.masamune.ui.model

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Equipment
import io.github.masamune.component.Graphic
import io.github.masamune.component.Inventory
import io.github.masamune.component.Inventory.Companion.addItem
import io.github.masamune.component.Inventory.Companion.removeItem
import io.github.masamune.component.Item
import io.github.masamune.component.Name
import io.github.masamune.component.Stats
import io.github.masamune.event.DialogBackEvent
import io.github.masamune.event.DialogOptionChangeEvent
import io.github.masamune.event.DialogOptionTriggerEvent
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.event.ShopBeginEvent
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.tiledmap.TiledService
import io.github.masamune.tiledmap.TiledStats
import ktx.log.logger

class ShopViewModel(
    bundle: I18NBundle,
    private val world: World,
    private val tiledService: TiledService,
    private val eventService: EventService = tiledService.eventService,
) : ViewModel(bundle) {

    private val weaponOption = ShopOption.WEAPON to bundle["menu.option.weapon"]
    private val armorOption = ShopOption.ARMOR to bundle["menu.option.armor"]
    private val accessoryOption = ShopOption.ACCESSORY to bundle["menu.option.accessory"]
    private val otherOption = ShopOption.OTHER to bundle["menu.option.item"]
    private val sellOption = ShopOption.SELL to bundle["menu.option.sell"]
    private val quitOption = ShopOption.QUIT to bundle["menu.option.quit"]

    // pair-left = localized text for UIStat, pair-right = value
    var playerStats: Map<UIStats, String> by propertyNotify(emptyMap())
    private var shopItems: Map<ItemCategory, List<ItemModel>> by propertyNotify(emptyMap())
    var options: List<Pair<ShopOption, String>> by propertyNotify(emptyList())
    var playerTalons: Int by propertyNotify(0)
    var totalCost: Int by propertyNotify(0)
    var shopName: String by propertyNotify("")
    private var activeItems: List<ItemModel> = emptyList()
    private var playerEntity = Entity.NONE
    var sellMode = false
        private set

    override fun onEvent(event: Event) {
        if (event !is ShopBeginEvent) {
            return
        }

        with(event.world) {
            // get player stats (this will update the ShopView)
            playerEntity = event.player
            playerStats = playerEntity[Stats].toUiMap()
            playerTalons = playerEntity[Inventory].talons

            // get shop items
            val shopEntity = event.shop
            shopName = shopEntity[Name].name
            shopItems = shopEntity[Inventory].items.groupBy(
                // group by item category
                { itemEntity -> itemEntity[Item].category }
            ) { it.toItemModel() }

            // get shop options like weapons, sell, etc. (this will update the ShopView)
            val optWeaponOption = if (shopItems.hasWeapon()) weaponOption else null
            val optArmorOption = if (shopItems.hasArmor()) armorOption else null
            val optAccessoryOption = if (shopItems.hasAccessory()) accessoryOption else null
            val optOtherOption = if (shopItems.hasOther()) otherOption else null
            options = listOfNotNull(
                optWeaponOption,
                optArmorOption,
                optAccessoryOption,
                optOtherOption,
                sellOption,
                quitOption
            )

            // reset total cost
            totalCost = 0
        }
    }

    private fun Entity.toItemModel(): ItemModel = with(world) {
        val itemEntity = this@toItemModel
        // and transform items into UI ItemModel objects
        val (type, cost, category, descriptionKey, amount) = itemEntity[Item]
        val itemName = itemEntity[Name].name
        val region: TextureRegion? = itemEntity.getOrNull(Graphic)?.region
        val itemStats = itemEntity.getOrNull(Stats)?.tiledStats ?: TiledStats.NULL_STATS

        val i18nName = bundle["item.$itemName.name"]
        val i18nDescription = bundle[descriptionKey]
        return ItemModel(
            type = type,
            stats = itemStats,
            name = i18nName,
            cost = cost,
            description = i18nDescription,
            category = category,
            image = TextureRegionDrawable(region),
            amount = amount,
        )
    }

    private fun Map<ItemCategory, List<ItemModel>>.hasWeapon(): Boolean {
        return ItemCategory.WEAPON in this
    }

    private fun Map<ItemCategory, List<ItemModel>>.hasArmor(): Boolean {
        return ItemCategory.ARMOR in this || ItemCategory.HELMET in this || ItemCategory.BOOTS in this
    }

    private fun Map<ItemCategory, List<ItemModel>>.hasAccessory(): Boolean {
        return ItemCategory.ACCESSORY in this
    }

    private fun Map<ItemCategory, List<ItemModel>>.hasOther(): Boolean {
        return ItemCategory.OTHER in this
    }

    fun shopItemsOf(vararg categories: ItemCategory): List<ItemModel> {
        activeItems = shopItems.filter { it.key in categories }.values.flatten()
        activeItems.forEach { it.selected = 0 }
        totalCost = 0
        sellMode = false
        return activeItems
    }

    fun incItemAmount(itemIdx: Int): Int = with(world) {
        val selectedItem = activeItems[itemIdx]
        if (sellMode) {
            if (selectedItem.selected < selectedItem.amount) {
                // this triggers a sound effect
                eventService.fire(DialogOptionChangeEvent)
                totalCost += selectedItem.cost
                ++selectedItem.selected
            }
            return@with selectedItem.selected
        }

        val talons = playerEntity[Inventory].talons
        if (talons < totalCost + selectedItem.cost) {
            // not enough talons to buy it
            return@with selectedItem.selected
        }

        // this triggers a sound effect
        eventService.fire(DialogOptionChangeEvent)
        totalCost += selectedItem.cost
        return ++selectedItem.selected
    }

    fun decItemAmount(itemIdx: Int): Int {
        val selectedItem = activeItems[itemIdx]
        if (selectedItem.selected == 0) {
            // cannot go below zero amount
            return 0
        }

        // this triggers a sound effect
        eventService.fire(DialogOptionChangeEvent)
        totalCost -= selectedItem.cost
        return --selectedItem.selected
    }

    fun itemsToSell(): List<ItemModel> = with(world) {
        activeItems = playerEntity[Inventory].items.map { it.toItemModel() }
        activeItems.forEach { it.selected = 0 }
        totalCost = 0
        sellMode = true
        return activeItems
    }

    private fun buyItems() = with(world) {
        // reduce player talons
        val inventoryCmp = playerEntity[Inventory]
        inventoryCmp.talons -= totalCost
        totalCost = 0
        playerTalons = inventoryCmp.talons

        // add items to inventory
        val itemsToBuy = activeItems.filter { it.selected > 0 }
        itemsToBuy.forEach { itemToBuy ->
            log.debug { "Adding item of type: ${itemToBuy.type}" }
            val itemToAdd = tiledService.loadItem(world, itemToBuy.type)
            itemToAdd[Item].amount = itemToBuy.selected
            world.addItem(itemToAdd, playerEntity)
        }
        log.debug { "New inventory:\n${inventoryCmp.items.map { it[Item] }.joinToString("\n")}" }

        // reset selected amounts of UI items
        activeItems.forEach { it.selected = 0 }
    }

    private fun sellItems() = with(world) {
        // increase player talons
        val inventoryCmp = playerEntity[Inventory]
        inventoryCmp.talons += totalCost
        totalCost = 0
        playerTalons = inventoryCmp.talons

        // remove items from inventory
        val itemsToSell = activeItems.filter { it.selected > 0 }
        itemsToSell.forEach { itemToSell ->
            log.debug { "Removing item of type: ${itemToSell.type}" }
            world.removeItem(itemToSell.type, itemToSell.selected, playerEntity)
        }
        log.debug { "New inventory:\n${inventoryCmp.items.map { it[Item] }.joinToString("\n")}" }
    }

    fun buyOrSellItems() = with(world) {
        if (sellMode) {
            sellItems()
            return@with
        }

        buyItems()
    }

    fun optionChanged() {
        // this triggers a sound effect
        eventService.fire(DialogOptionChangeEvent)
    }

    fun optionOrItemSelected() {
        // this triggers a sound effect
        eventService.fire(DialogOptionTriggerEvent)
    }

    fun optionCancelled() {
        // this triggers a sound effect
        eventService.fire(DialogBackEvent)
    }

    fun calcDiff(selectedItem: ItemModel): Map<UIStats, Int> {
        if (!selectedItem.category.isEquipment) {
            return emptyMap()
        }

        with(world) {
            val (items) = playerEntity[Equipment]
            val itemToCompare = items.firstOrNull { it[Item].category == selectedItem.category }
            val selectedStats = selectedItem.stats

            if (itemToCompare == null) {
                // player has no item of given type selected
                // -> diff is the stats of the selected item
                return mapOf(
                    UIStats.STRENGTH to selectedStats.strength.toInt(),
                    UIStats.AGILITY to selectedStats.agility.toInt(),
                    UIStats.CONSTITUTION to selectedStats.constitution.toInt(),
                    UIStats.INTELLIGENCE to selectedStats.intelligence.toInt(),
                    UIStats.DAMAGE to selectedStats.damage.toInt(),
                    UIStats.ARMOR to selectedStats.armor.toInt(),
                    UIStats.RESISTANCE to selectedStats.resistance.toInt(),
                )
            }

            // compare selected item with currently equipped item
            val equipStats = itemToCompare[Stats].tiledStats
            return mapOf(
                UIStats.STRENGTH to (equipStats.strength - selectedStats.strength).toInt(),
                UIStats.AGILITY to (equipStats.agility - selectedStats.agility).toInt(),
                UIStats.CONSTITUTION to (equipStats.constitution - selectedStats.constitution).toInt(),
                UIStats.INTELLIGENCE to (equipStats.intelligence - selectedStats.intelligence).toInt(),
                UIStats.DAMAGE to (equipStats.damage - selectedStats.damage).toInt(),
                UIStats.ARMOR to (equipStats.armor - selectedStats.armor).toInt(),
                UIStats.RESISTANCE to (equipStats.resistance - selectedStats.resistance).toInt(),
            )
        }
    }

    companion object {
        private val log = logger<ShopViewModel>()
    }
}
