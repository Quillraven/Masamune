package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.ui.model.ItemModel
import io.github.masamune.ui.model.ShopOption
import io.github.masamune.ui.model.ShopViewModel
import io.github.masamune.ui.model.UIStats
import io.github.masamune.ui.widget.ItemInfoTable
import io.github.masamune.ui.widget.ItemTable
import io.github.masamune.ui.widget.OptionTable
import io.github.masamune.ui.widget.ShopStatsLabel
import io.github.masamune.ui.widget.frameImage
import io.github.masamune.ui.widget.itemInfoTable
import io.github.masamune.ui.widget.itemTable
import io.github.masamune.ui.widget.optionTable
import io.github.masamune.ui.widget.shopStatsLabel
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import ktx.scene2d.label
import ktx.scene2d.scene2d
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

    private val strLabel: Label
    private val strShopStatsLabel: ShopStatsLabel
    private val agiLabel: Label
    private val agiShopStatsLabel: ShopStatsLabel
    private val intLabel: Label
    private val intShopStatsLabel: ShopStatsLabel
    private val constLabel: Label
    private val constShopStatsLabel: ShopStatsLabel
    private val attackLabel: Label
    private val attackShopStatsLabel: ShopStatsLabel
    private val armorLabel: Label
    private val armorShopStatsLabel: ShopStatsLabel
    private val resistanceLabel: Label
    private val resistanceShopStatsLabel: ShopStatsLabel

    private val itemTable: ItemTable
    private val optionTable: OptionTable
    private val itemInfoTable: ItemInfoTable

    private var focus = ShopViewFocus.OPTIONS
    private var activeItems: List<ItemModel> = emptyList()

    init {
        background = skin.getDrawable("dialog_frame")
        setFillParent(true)

        initTopLeft(skin)
        initTopRight(skin)
        initBottomLeft(skin)
        initBottomRight(skin)

        talonLabel = findActor(ShopView::talonLabel.name)
        totalLabel = findActor(ShopView::totalLabel.name)

        strLabel = findActor(ShopView::strLabel.name)
        strShopStatsLabel = findActor(ShopView::strShopStatsLabel.name)
        agiLabel = findActor(ShopView::agiLabel.name)
        agiShopStatsLabel = findActor(ShopView::agiShopStatsLabel.name)
        intLabel = findActor(ShopView::intLabel.name)
        intShopStatsLabel = findActor(ShopView::intShopStatsLabel.name)
        constLabel = findActor(ShopView::constLabel.name)
        constShopStatsLabel = findActor(ShopView::constShopStatsLabel.name)
        attackLabel = findActor(ShopView::attackLabel.name)
        attackShopStatsLabel = findActor(ShopView::attackShopStatsLabel.name)
        armorLabel = findActor(ShopView::armorLabel.name)
        armorShopStatsLabel = findActor(ShopView::armorShopStatsLabel.name)
        resistanceLabel = findActor(ShopView::resistanceLabel.name)
        resistanceShopStatsLabel = findActor(ShopView::resistanceShopStatsLabel.name)

        itemTable = findActor(ShopView::itemTable.name)
        optionTable = findActor(ShopView::optionTable.name)
        itemInfoTable = findActor(ShopView::itemInfoTable.name)

        registerOnPropertyChanges(model)
    }

    private fun statsRow(table: Table, skin: Skin, labelName: String, statsName: String) {
        val label = scene2d.label("", defaultStyle, skin) {
            color = skin.getColor("dark_grey")
            setAlignment(Align.left)
            name = labelName
        }
        val statsLabel = scene2d.shopStatsLabel(skin, "") {
            name = statsName
        }
        table.add(label).left()
        table.add(statsLabel).left().padLeft(50f).fillX().row()
    }

    private fun initTopLeft(skin: Skin) {
        table(skin) { tlTableCell ->

            // header: Shop Name + character faces
            label("Merchant", "dialog_image_caption", skin) {
                setAlignment(Align.center)
            }
            frameImage(skin, "dialog_face_frame", "hero") { cell ->
                cell.row()
            }

            // character stats
            this@ShopView.statsRow(this, skin, ShopView::strLabel.name, ShopView::strShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::agiLabel.name, ShopView::agiShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::constLabel.name, ShopView::constShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::intLabel.name, ShopView::intShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::attackLabel.name, ShopView::attackShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::armorLabel.name, ShopView::armorShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::resistanceLabel.name, ShopView::resistanceShopStatsLabel.name)

            tlTableCell.padLeft(10.0f).padTop(10f).top().left()
        }
    }

    private fun initTopRight(skin: Skin) {
        table(skin) { tblCell ->
            background = skin.getDrawable("dialog_frame")

            optionTable(skin) { optionTableCell ->
                optionTableCell.fill().align(Align.left)
                name = ShopView::optionTable.name
            }

            tblCell.top().right().padTop(10f).padRight(10f).width(250f).row()
        }
    }

    private fun initBottomLeft(skin: Skin) {
        table(skin) { tblCell ->
            background = skin.getDrawable("dialog_frame")

            table(skin) { innerTblCell ->
                label("", "dialog_option", skin) { lblCell ->
                    setAlignment(Align.center)
                    name = ShopView::talonLabel.name
                    lblCell.padTop(2f).padBottom(5f).expandX().left()
                }
                label("", "dialog_option", skin) { lblCell ->
                    setAlignment(Align.center)
                    name = ShopView::totalLabel.name
                    lblCell.padTop(2f).padBottom(5f)
                }
                innerTblCell.growX().row()
            }

            itemTable(skin) { itCell ->
                this.name = ShopView::itemTable.name
                this.isVisible = false
                itCell.grow()
            }

            tblCell.pad(10f, 10f, 10f, 20f).grow()
        }
    }

    private fun initBottomRight(skin: Skin) {
        itemInfoTable(skin) { tblCell ->
            name = ShopView::itemInfoTable.name
            isVisible = false
            tblCell.pad(10f, 0f, 10f, 10f).growX().top()
        }
    }

    private fun registerOnPropertyChanges(model: ShopViewModel) {
        model.onPropertyChange(ShopViewModel::playerStats) { labelsAndStats ->
            val errorTitleLabel = "???" to "0"

            // money
            val talonsTitle = labelsAndStats[UIStats.TALONS]?.first ?: errorTitleLabel.first
            val talonsValue = labelsAndStats[UIStats.TALONS]?.second ?: errorTitleLabel.second
            talonLabel.setText(" $talonsTitle: $talonsValue$TALONS_POSTFIX ")
            // stats
            strLabel.setText(labelsAndStats[UIStats.STRENGTH]?.first ?: errorTitleLabel.first)
            strShopStatsLabel.txt(labelsAndStats[UIStats.STRENGTH]?.second ?: errorTitleLabel.second, 1)
            agiLabel.setText(labelsAndStats[UIStats.AGILITY]?.first ?: errorTitleLabel.first)
            agiShopStatsLabel.txt(labelsAndStats[UIStats.AGILITY]?.second ?: errorTitleLabel.second, 2)
            intLabel.setText(labelsAndStats[UIStats.INTELLIGENCE]?.first ?: errorTitleLabel.first)
            intShopStatsLabel.txt(labelsAndStats[UIStats.INTELLIGENCE]?.second ?: errorTitleLabel.second, 300)
            constLabel.setText(labelsAndStats[UIStats.CONSTITUTION]?.first ?: errorTitleLabel.first)
            constShopStatsLabel.txt(labelsAndStats[UIStats.CONSTITUTION]?.second ?: errorTitleLabel.second, 4)

            attackLabel.setText(labelsAndStats[UIStats.ATTACK]?.first ?: errorTitleLabel.first)
            attackShopStatsLabel.txt(labelsAndStats[UIStats.ATTACK]?.second ?: errorTitleLabel.second, 0)
            armorLabel.setText(labelsAndStats[UIStats.ARMOR]?.first ?: errorTitleLabel.first)
            armorShopStatsLabel.txt(labelsAndStats[UIStats.ARMOR]?.second ?: errorTitleLabel.second, -200)
            resistanceLabel.setText(labelsAndStats[UIStats.RESISTANCE]?.first ?: errorTitleLabel.first)
            resistanceShopStatsLabel.txt(labelsAndStats[UIStats.RESISTANCE]?.second ?: errorTitleLabel.second, -3)
        }

        model.onPropertyChange(ShopViewModel::options) { optionNames ->
            optionNames.forEach { optionTable.option(it.second, it.first) }
        }

        model.onPropertyChange(ShopViewModel::totalCost) { labelAndValues ->
            totalLabel.setText(" ${labelAndValues.first}: ${labelAndValues.second}$TALONS_POSTFIX ")
        }
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
