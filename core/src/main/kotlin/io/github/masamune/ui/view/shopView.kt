package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.ui.model.ItemModel
import io.github.masamune.ui.model.ShopOption
import io.github.masamune.ui.model.ShopViewModel
import io.github.masamune.ui.model.UIStats
import io.github.masamune.ui.widget.ItemInfoTable
import io.github.masamune.ui.widget.ItemTable
import io.github.masamune.ui.widget.OptionTable
import io.github.masamune.ui.widget.ShopStatsTable
import io.github.masamune.ui.widget.itemInfoTable
import io.github.masamune.ui.widget.itemTable
import io.github.masamune.ui.widget.optionTable
import io.github.masamune.ui.widget.shopStatsTable
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table


private enum class ShopViewFocus {
    OPTIONS, ITEMS
}

@Scene2dDsl
class ShopView(
    model: ShopViewModel,
    skin: Skin,
) : View<ShopViewModel>(skin, model), KTable {

    private val talonLabel: Label
    private val totalLabel: Label

    private val shopStatsTable: ShopStatsTable
    private val itemTable: ItemTable
    private val optionTable: OptionTable
    private val itemInfoTable: ItemInfoTable

    private var focus = ShopViewFocus.OPTIONS
    private var activeItems: List<ItemModel> = emptyList()

    init {
        background = skin.getDrawable("dialog_frame")
        setFillParent(true)
        val uiStatsLabels = viewModel.statsLabels()

        // top left -> shop title and stats
        shopStatsTable = shopStatsTable(skin, "", uiStatsLabels) {
            it.padLeft(10.0f).padTop(10f).top().left()
        }
        // top right -> shop options
        table(skin) { tblCell ->
            background = skin.getDrawable("dialog_frame")

            this@ShopView.optionTable = optionTable(skin) { optionTableCell ->
                optionTableCell.fill().align(Align.left)
            }

            tblCell.top().right().padTop(10f).padRight(10f).width(250f).row()
        }
        // bottom left -> scrollable items + talons
        table(skin) { tblCell ->
            background = skin.getDrawable("dialog_frame")

            table(skin) { innerTblCell ->
                val talonsLabel = uiStatsLabels.of(UIStats.TALONS)
                this@ShopView.talonLabel = label(talonsLabel, "dialog_option", skin) { lblCell ->
                    userObject = talonsLabel
                    setAlignment(Align.center)
                    lblCell.padTop(2f).padBottom(5f).expandX().left()
                }
                this@ShopView.totalLabel = label("", "dialog_option", skin) { lblCell ->
                    setAlignment(Align.center)
                    lblCell.padTop(2f).padBottom(5f)
                }
                innerTblCell.growX().row()
            }

            this@ShopView.itemTable = itemTable(skin) { itCell ->
                isVisible = false
                itCell.grow()
            }

            tblCell.pad(10f, 10f, 10f, 20f).grow()
        }
        // bottom right -> item info
        itemInfoTable = itemInfoTable(skin) { tblCell ->
            isVisible = false
            tblCell.pad(10f, 0f, 10f, 10f).growX().top()
        }

        registerOnPropertyChanges(model)
    }

    private fun registerOnPropertyChanges(model: ShopViewModel) {
        model.onPropertyChange(ShopViewModel::playerStats) { stats ->
            val missingValue = "0"

            // money
            val talonsValue = stats[UIStats.TALONS] ?: missingValue
            talonLabel.setText(" ${talonLabel.userObject}: $talonsValue$TALONS_POSTFIX ")
            // stats
            shopStatsTable.statsValue(UIStats.STRENGTH, stats[UIStats.STRENGTH] ?: missingValue, 1)
            shopStatsTable.statsValue(UIStats.AGILITY, stats[UIStats.AGILITY] ?: missingValue, 2)
            shopStatsTable.statsValue(UIStats.INTELLIGENCE, stats[UIStats.INTELLIGENCE] ?: missingValue, 300)
            shopStatsTable.statsValue(UIStats.CONSTITUTION, stats[UIStats.CONSTITUTION] ?: missingValue, 4)
            shopStatsTable.statsValue(UIStats.ATTACK, stats[UIStats.ATTACK] ?: missingValue)
            shopStatsTable.statsValue(UIStats.ARMOR, stats[UIStats.ARMOR] ?: missingValue, -200)
            shopStatsTable.statsValue(UIStats.RESISTANCE, stats[UIStats.RESISTANCE] ?: missingValue, -3)
        }

        model.onPropertyChange(ShopViewModel::options) { optionNames ->
            optionNames.forEach { optionTable.option(it.second, it.first) }
        }

        model.onPropertyChange(ShopViewModel::totalCost) { labelAndValues ->
            totalLabel.setText(" ${labelAndValues.first}: ${labelAndValues.second}$TALONS_POSTFIX ")
        }

        model.onPropertyChange(ShopViewModel::shopName) { shopStatsTable.shopName(it)}
    }

    private fun updateActiveItem() {
        if (activeItems.isEmpty()) {
            return
        }

        val selectedItem = activeItems[itemTable.selectedItem]
        itemInfoTable.item(selectedItem.name, selectedItem.description, selectedItem.image)
    }

    override fun onUpPressed() {
        when (focus) {
            ShopViewFocus.OPTIONS -> optionTable.prevOption()
            ShopViewFocus.ITEMS -> {
                itemTable.prevItem()
                updateActiveItem()
            }
        }
    }

    override fun onDownPressed() {
        when (focus) {
            ShopViewFocus.OPTIONS -> optionTable.nextOption()
            ShopViewFocus.ITEMS -> {
                itemTable.nextItem()
                updateActiveItem()
            }
        }
    }

    private fun selectOption() {
        val shopOption: ShopOption = optionTable.selectedUserObject()
        activeItems = when (shopOption) {
            ShopOption.WEAPON -> viewModel.shopItemsOf(ItemCategory.WEAPON)
            ShopOption.ARMOR -> viewModel.shopItemsOf(ItemCategory.ARMOR, ItemCategory.HELMET, ItemCategory.BOOTS)
            ShopOption.ACCESSORY -> viewModel.shopItemsOf(ItemCategory.ACCESSORY)
            ShopOption.OTHER -> viewModel.shopItemsOf(ItemCategory.OTHER)
            ShopOption.SELL -> viewModel.sellItems()
            ShopOption.QUIT -> {
                println("TODO QUIT")
                return
            }
        }

        focus = ShopViewFocus.ITEMS
        optionTable.stopSelectAnimation()
        itemTable.isVisible = true
        itemInfoTable.isVisible = activeItems.isNotEmpty()
        itemTable.clearItems()
        itemInfoTable.clearItem()
        activeItems.forEach { itemTable.item(itemName(it.name), it.cost) }
        updateActiveItem()
    }

    // shorten item names by a maximum length
    private fun itemName(name: String): String {
        if (name.length > 14) {
            return "${name.substring(0, 14)}."
        }
        return name
    }

    override fun onSelectPressed() {
        when (focus) {
            ShopViewFocus.OPTIONS -> selectOption()
            ShopViewFocus.ITEMS -> Unit
        }
    }

    override fun onBackPressed() {
        when (focus) {
            ShopViewFocus.OPTIONS -> optionTable.lastOption() // select 'Quit' option
            ShopViewFocus.ITEMS -> {
                focus = ShopViewFocus.OPTIONS
                itemTable.clearItems()
                itemTable.isVisible = false
                itemInfoTable.isVisible = false
                optionTable.resumeSelectAnimation()
            }
        }
    }

    companion object {
        const val TALONS_POSTFIX = "[#FFFFFF77]K[]" // postfix at the end of an item cost
    }
}

@Scene2dDsl
fun <S> KWidget<S>.shopView(
    model: ShopViewModel,
    skin: Skin,
    init: (@Scene2dDsl ShopView).(S) -> Unit = {},
): ShopView = actor(ShopView(model, skin), init)
