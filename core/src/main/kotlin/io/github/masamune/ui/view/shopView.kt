package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.ui.model.I18NKey
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
import ktx.scene2d.defaultStyle
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table


private enum class ShopViewFocus {
    OPTIONS, ITEMS, CONFIRM
}

@Scene2dDsl
class ShopView(
    model: ShopViewModel,
    private val skin: Skin,
) : View<ShopViewModel>(skin, model), KTable {

    private val talonLabel: Label
    private val totalLabel: Label

    private val shopStatsTable: ShopStatsTable
    private val itemTable: ItemTable
    private val optionTable: OptionTable
    private val itemInfoTable: ItemInfoTable
    private val confirmTable: Table
    private val confirmOptionTable: OptionTable

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
                    setAlignment(Align.left)
                    lblCell.padTop(2f).padBottom(5f).expandX().left().minWidth(220f)
                }
                val totalLabel = model.totalLabel()
                this@ShopView.totalLabel = label(totalLabel, "dialog_option", skin) { lblCell ->
                    userObject = totalLabel
                    setAlignment(Align.left)
                    lblCell.padTop(2f).padBottom(5f).minWidth(220f)
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
        // confirm buy popup
        confirmTable = scene2d.table(this@ShopView.skin) {
            setFillParent(true)

            table(skin) {
                background = skin.getDrawable("dialog_frame")

                label(this@ShopView.viewModel.labelTxt(I18NKey.SHOP_CONFIRM_BUY), defaultStyle, skin) {
                    color = this@ShopView.skin.getColor("dark_grey")
                    it.grow().colspan(2).pad(2f, 2f, 5f, 2f).row()
                }
                this@ShopView.confirmOptionTable = optionTable(skin) {
                    option(this@ShopView.viewModel.labelTxt(I18NKey.GENERAL_YES))
                    option(this@ShopView.viewModel.labelTxt(I18NKey.GENERAL_NO))
                }

                center()
            }
        }

        registerOnPropertyChanges(model)
    }

    private fun registerOnPropertyChanges(model: ShopViewModel) {
        model.onPropertyChange(ShopViewModel::playerStats) { stats ->
            val missingValue = "0"

            shopStatsTable.statsValue(UIStats.STRENGTH, stats[UIStats.STRENGTH] ?: missingValue, 1)
            shopStatsTable.statsValue(UIStats.AGILITY, stats[UIStats.AGILITY] ?: missingValue, 2)
            shopStatsTable.statsValue(UIStats.INTELLIGENCE, stats[UIStats.INTELLIGENCE] ?: missingValue, 300)
            shopStatsTable.statsValue(UIStats.CONSTITUTION, stats[UIStats.CONSTITUTION] ?: missingValue, 4)
            shopStatsTable.statsValue(UIStats.ATTACK, stats[UIStats.ATTACK] ?: missingValue)
            shopStatsTable.statsValue(UIStats.ARMOR, stats[UIStats.ARMOR] ?: missingValue, -200)
            shopStatsTable.statsValue(UIStats.RESISTANCE, stats[UIStats.RESISTANCE] ?: missingValue, -3)
        }

        model.onPropertyChange(ShopViewModel::playerTalons) { value ->
            talonLabel.setText(" ${talonLabel.userObject}: $value$TALONS_POSTFIX ")
        }

        model.onPropertyChange(ShopViewModel::options) { optionNames ->
            optionNames.forEach { optionTable.option(it.second, it.first) }
        }

        model.onPropertyChange(ShopViewModel::totalCost) { value ->
            totalLabel.setText(" ${totalLabel.userObject}: ${value}$TALONS_POSTFIX ")
        }

        model.onPropertyChange(ShopViewModel::shopName) { shopStatsTable.shopName(it) }
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
            ShopViewFocus.OPTIONS -> {
                if (optionTable.prevOption()) {
                    viewModel.optionChanged()
                }
            }

            ShopViewFocus.ITEMS -> {
                if (itemTable.prevItem()) {
                    viewModel.optionChanged()
                    updateActiveItem()
                }
            }

            ShopViewFocus.CONFIRM -> {
                if (confirmOptionTable.prevOption()) {
                    viewModel.optionChanged()
                }
            }
        }
    }

    override fun onDownPressed() {
        when (focus) {
            ShopViewFocus.OPTIONS -> {
                if (optionTable.nextOption()) {
                    viewModel.optionChanged()
                }
            }

            ShopViewFocus.ITEMS -> {
                if (itemTable.nextItem()) {
                    viewModel.optionChanged()
                    updateActiveItem()
                }
            }

            ShopViewFocus.CONFIRM -> {
                if (confirmOptionTable.nextOption()) {
                    viewModel.optionChanged()
                }
            }
        }
    }

    override fun onRightPressed() {
        if (focus != ShopViewFocus.ITEMS || itemTable.hasNoItems()) {
            return
        }

        val amount = viewModel.selectItem(itemTable.selectedItem)
        itemTable.amount(amount)
    }

    override fun onLeftPressed() {
        if (focus != ShopViewFocus.ITEMS || itemTable.hasNoItems()) {
            return
        }

        val amount = viewModel.deselectItem(itemTable.selectedItem)
        itemTable.amount(amount)
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

    private fun onBuyItems() {
        if (viewModel.totalCost == 0) {
            return
        }

        viewModel.optionOrItemSelected()
        focus = ShopViewFocus.CONFIRM
        itemTable.stopSelectAnimation()
        confirmOptionTable.firstOption()
        stage.addActor(confirmTable)
    }

    override fun onSelectPressed() {
        when (focus) {
            ShopViewFocus.OPTIONS -> {
                selectOption()
                viewModel.optionOrItemSelected()
            }

            ShopViewFocus.ITEMS -> onBuyItems()
            ShopViewFocus.CONFIRM -> {
                closeConfirmPopup()
                if (confirmOptionTable.selectedOption == 0) {
                    // item buy confirmed
                    viewModel.optionOrItemSelected()
                    viewModel.buyItems()
                    itemTable.clearAmounts()
                    return
                }
                viewModel.optionCancelled()
            }
        }
    }

    override fun onBackPressed() {
        when (focus) {
            ShopViewFocus.OPTIONS -> {
                // select 'Quit' option
                if (optionTable.lastOption()) {
                    viewModel.optionChanged()
                }
            }

            ShopViewFocus.ITEMS -> {
                focus = ShopViewFocus.OPTIONS
                itemTable.clearItems()
                itemTable.isVisible = false
                itemInfoTable.isVisible = false
                optionTable.resumeSelectAnimation()
                viewModel.optionCancelled()
            }

            ShopViewFocus.CONFIRM -> {
                closeConfirmPopup()
                viewModel.optionCancelled()
            }
        }
    }

    private fun closeConfirmPopup() {
        focus = ShopViewFocus.ITEMS
        confirmTable.remove()
        itemTable.resumeSelectAnimation()
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
