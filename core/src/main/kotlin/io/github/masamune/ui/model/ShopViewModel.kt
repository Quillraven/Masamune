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
    private val bundle: I18NBundle,
    private val world: World,
) : ViewModel() {

    // pair-left = localized text for UIStat, pair-right = value
    var playerStats: Map<UIStats, Pair<String, String>> by propertyNotify(emptyMap())
    var shopItems: Map<ItemCategory, List<ItemModel>> by propertyNotify(emptyMap())
    var options: List<String> by propertyNotify(emptyList())

    override fun onEvent(event: Event) {
        if (event !is ShopBeginEvent) {
            return
        }

        with(event.world) {
            // get player stats (this will update the ShopView)
            val player = event.player
            playerStats = player[Stats].toUiMap(bundle) and player[Inventory].toUiMap(bundle)

            // get shop items (this will update the ShopView)
            val shop = event.shop
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
            val weaponOption = if (shopItems.hasWeapon()) bundle["menu.option.weapon"] else null
            val armorOption = if (shopItems.hasArmor()) bundle["menu.option.armor"] else null
            val accessoryOption = if (shopItems.hasAccessory()) bundle["menu.option.accessory"] else null
            val itemOption = if (shopItems.hasOther()) bundle["menu.option.item"] else null
            val sellOption = bundle["menu.option.sell"]
            val quitOption = bundle["menu.option.quit"]
            options = listOfNotNull(weaponOption, armorOption, accessoryOption, itemOption, sellOption, quitOption)
        }
    }

    private fun Map<ItemCategory, List<ItemModel>>.hasWeapon(): Boolean {
        return ItemCategory.WEAPON in shopItems
    }

    private fun Map<ItemCategory, List<ItemModel>>.hasArmor(): Boolean {
        return ItemCategory.ARMOR in shopItems || ItemCategory.HELMET in shopItems || ItemCategory.BOOTS in shopItems
    }

    private fun Map<ItemCategory, List<ItemModel>>.hasAccessory(): Boolean {
        return ItemCategory.ACCESSORY in shopItems
    }

    private fun Map<ItemCategory, List<ItemModel>>.hasOther(): Boolean {
        return ItemCategory.OTHER in shopItems
    }

}
