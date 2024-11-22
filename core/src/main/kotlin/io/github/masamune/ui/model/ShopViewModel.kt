package io.github.masamune.ui.model

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.World
import io.github.masamune.component.Graphic
import io.github.masamune.component.Inventory
import io.github.masamune.component.Item
import io.github.masamune.component.Name
import io.github.masamune.component.Stats
import io.github.masamune.event.Event
import io.github.masamune.event.ShopBeginEvent
import io.github.masamune.tiledmap.ItemCategory

class ShopViewModel(
    bundle: I18NBundle,
    private val world: World,
) : ViewModel(bundle) {

    private val weaponOption = ShopOption.WEAPON to bundle["menu.option.weapon"]
    private val armorOption = ShopOption.ARMOR to bundle["menu.option.armor"]
    private val accessoryOption = ShopOption.ACCESSORY to bundle["menu.option.accessory"]
    private val otherOption = ShopOption.OTHER to bundle["menu.option.item"]
    private val sellOption = ShopOption.SELL to bundle["menu.option.sell"]
    private val quitOption = ShopOption.QUIT to bundle["menu.option.quit"]

    // pair-left = localized text for UIStat, pair-right = value
    var playerStats: Map<UIStats, String> by propertyNotify(emptyMap())
    var shopItems: Map<ItemCategory, List<ItemModel>> by propertyNotify(emptyMap())
    var options: List<Pair<ShopOption, String>> by propertyNotify(emptyList())
    var totalCost: Pair<String, Int> by propertyNotify("" to 0)
    var shopName: String by propertyNotify("")

    override fun onEvent(event: Event) {
        if (event !is ShopBeginEvent) {
            return
        }

        with(event.world) {
            // get player stats (this will update the ShopView)
            val player = event.player
            playerStats = player[Stats].toUiMap(bundle) and player[Inventory].toUiMap(bundle)

            // get shop items
            val shop = event.shop
            shopName = shop[Name].name
            shopItems = shop[Inventory].items.groupBy(
                // group by item category
                { itemEntity -> itemEntity[Item].category }
            ) { itemEntity ->
                // and transform items into UI ItemModel objects
                val (cost, category, descriptionKey) = itemEntity[Item]
                val itemName = itemEntity[Name].name
                val region: TextureRegion? = itemEntity.getOrNull(Graphic)?.region

                val i18nName = bundle["item.$itemName.name"]
                val i18nDescription = bundle[descriptionKey]
                ItemModel(
                    name = i18nName,
                    cost = cost,
                    description = i18nDescription,
                    category = category,
                    image = TextureRegionDrawable(region)
                )
            }

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
            totalCost = bundle["general.total"] to 0
        }
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
        return shopItems.filter { it.key in categories }.values.flatten()
    }

    fun sellItems(): List<ItemModel> {
        return emptyList()
    }

}
