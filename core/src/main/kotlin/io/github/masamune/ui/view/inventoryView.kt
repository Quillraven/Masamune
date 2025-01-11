package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.ui.model.I18NKey
import io.github.masamune.ui.model.InventoryViewModel
import io.github.masamune.ui.model.ItemModel
import io.github.masamune.ui.model.UIStats
import io.github.masamune.ui.widget.EquipmentStatsTable
import io.github.masamune.ui.widget.ItemInfoTable
import io.github.masamune.ui.widget.ItemInventoryTable
import io.github.masamune.ui.widget.OptionTable
import io.github.masamune.ui.widget.equipmentStatsTable
import io.github.masamune.ui.widget.itemInfoTable
import io.github.masamune.ui.widget.itemInventoryTable
import io.github.masamune.ui.widget.optionTable
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.table

private enum class InventoryUiState {
    SELECT_OPTION, ITEMS, EQUIPMENT
}

@Scene2dDsl
class InventoryView(
    model: InventoryViewModel,
    skin: Skin,
) : View<InventoryViewModel>(skin, model), KTable {

    private val equipmentStatsTable: EquipmentStatsTable
    private val optionTable: OptionTable
    private val itemTable: ItemInventoryTable
    private val itemInfoTable: ItemInfoTable
    private var state = InventoryUiState.SELECT_OPTION

    init {
        background = skin.getDrawable("dialog_frame")
        setFillParent(true)

        val uiStatsLabels = mapOf(
            UIStats.STRENGTH to i18nTxt(I18NKey.STATS_STRENGTH),
            UIStats.AGILITY to i18nTxt(I18NKey.STATS_AGILITY),
            UIStats.CONSTITUTION to i18nTxt(I18NKey.STATS_CONSTITUTION),
            UIStats.INTELLIGENCE to i18nTxt(I18NKey.STATS_INTELLIGENCE),
            UIStats.DAMAGE to i18nTxt(I18NKey.STATS_ATTACK),
            UIStats.ARMOR to i18nTxt(I18NKey.STATS_ARMOR),
            UIStats.RESISTANCE to i18nTxt(I18NKey.STATS_RESISTANCE),
        )
        val uiEquipmentLabels = mapOf(
            ItemCategory.WEAPON to i18nTxt(I18NKey.ITEM_CATEGORY_WEAPON),
            ItemCategory.ARMOR to i18nTxt(I18NKey.ITEM_CATEGORY_ARMOR),
            ItemCategory.HELMET to i18nTxt(I18NKey.ITEM_CATEGORY_HELMET),
            ItemCategory.BOOTS to i18nTxt(I18NKey.ITEM_CATEGORY_BOOTS),
            ItemCategory.ACCESSORY to i18nTxt(I18NKey.ITEM_CATEGORY_ACCESSORY),
        )
        val title = i18nTxt(I18NKey.MENU_OPTION_ITEM)
        val options = listOf(
            i18nTxt(I18NKey.MENU_OPTION_ITEM),
            i18nTxt(I18NKey.MENU_OPTION_EQUIPMENT),
            i18nTxt(I18NKey.MENU_OPTION_QUIT),
        )

        // top left -> title, equipment and stats
        equipmentStatsTable = equipmentStatsTable(skin, title, uiEquipmentLabels, uiStatsLabels) {
            it.growX().align(Align.topLeft).padBottom(70f)
        }

        // top right -> inventory options
        table(skin) { tblCell ->
            background = skin.getDrawable("dialog_frame")
            align(Align.topRight)

            this@InventoryView.optionTable = optionTable(skin, options)

            tblCell.top().right().padRight(10f).row()
        }

        // bottom left -> scrollable items + talons
        table(skin) { tblCell ->
            background = skin.getDrawable("dialog_frame")

            this@InventoryView.itemTable = itemInventoryTable(skin) {
                isVisible = false
                it.grow()
            }

            tblCell.pad(10f, 10f, 10f, 0f).growY().left().minWidth(450f)
        }

        // bottom right -> item info
        itemInfoTable = itemInfoTable(skin) { tblCell ->
            isVisible = false
            tblCell.fillX().top().width(385f).pad(10f, 0f, 0f, 10f)
        }

        registerOnPropertyChanges()
    }

    override fun registerOnPropertyChanges() {
        viewModel.onPropertyChange(InventoryViewModel::playerName) { name ->
            equipmentStatsTable.playerName(name)
        }
        viewModel.onPropertyChange(InventoryViewModel::playerStats) { stats ->
            equipmentStatsTable.statsValue(UIStats.STRENGTH, stats[UIStats.STRENGTH] ?: "0")
            equipmentStatsTable.statsValue(UIStats.AGILITY, stats[UIStats.AGILITY] ?: "0")
            equipmentStatsTable.statsValue(UIStats.CONSTITUTION, stats[UIStats.CONSTITUTION] ?: "0")
            equipmentStatsTable.statsValue(UIStats.INTELLIGENCE, stats[UIStats.INTELLIGENCE] ?: "0")
            equipmentStatsTable.statsValue(UIStats.DAMAGE, stats[UIStats.DAMAGE] ?: "0")
            equipmentStatsTable.statsValue(UIStats.ARMOR, stats[UIStats.ARMOR] ?: "0")
            equipmentStatsTable.statsValue(UIStats.RESISTANCE, stats[UIStats.RESISTANCE] ?: "0")
        }
    }

    override fun onUpPressed() {
        when (state) {
            InventoryUiState.SELECT_OPTION -> {
                if (optionTable.prevOption()) {
                    viewModel.playSndMenuClick()
                }
            }

            InventoryUiState.ITEMS -> {
                if (itemTable.prevEntry()) {
                    val item = viewModel.item(itemTable.selectedEntryIdx)
                    itemInfoTable.item(item.name, item.description, item.image)
                    viewModel.playSndMenuClick()
                }
            }

            InventoryUiState.EQUIPMENT -> {
                if (itemTable.prevEntry()) {
                    updateEquipment()
                }
            }
        }
    }

    override fun onDownPressed() {
        when (state) {
            InventoryUiState.SELECT_OPTION -> {
                if (optionTable.nextOption()) {
                    viewModel.playSndMenuClick()
                }
            }

            InventoryUiState.ITEMS -> {
                if (itemTable.nextEntry()) {
                    val item = viewModel.item(itemTable.selectedEntryIdx)
                    itemInfoTable.item(item.name, item.description, item.image)
                    viewModel.playSndMenuClick()
                }
            }

            InventoryUiState.EQUIPMENT -> {
                if (itemTable.nextEntry()) {
                    updateEquipment()
                }
            }
        }
    }

    private fun updateEquipment() {
        val item = viewModel.inventoryEquipment(itemTable.selectedEntryIdx)
        itemInfoTable.item(item.name, item.description, item.image)
        viewModel.playSndMenuClick()

        val diff: Map<UIStats, Int> = viewModel.calcDiff(itemTable.selectedEntryIdx)
        equipmentStatsTable.clearDiff()
        diff.forEach { (uiStat, diffValue) ->
            equipmentStatsTable.diffValue(uiStat, diffValue)
        }
    }

    override fun onSelectPressed() {
        when (state) {
            InventoryUiState.SELECT_OPTION -> {
                when (optionTable.selectedOption) {
                    0 -> {
                        state = InventoryUiState.ITEMS
                        optionTable.stopSelectAnimation()

                        // update item table and item info table
                        updateItemTableAndInfo(viewModel.items())

                        viewModel.playSndMenuAccept()
                    }

                    1 -> {
                        state = InventoryUiState.EQUIPMENT
                        optionTable.stopSelectAnimation()

                        // update equipment info
                        viewModel.playerEquipment().forEach { (category, item) ->
                            equipmentStatsTable.equipmentName(category, item.name)
                        }
                        equipmentStatsTable.showEquipment(true)

                        // update item table and item info table
                        updateItemTableAndInfo(viewModel.inventoryEquipment())

                        // update diff
                        updateEquipment()

                        viewModel.playSndMenuAccept()
                    }

                    else -> {
                        viewModel.quit()
                    }
                }
            }

            InventoryUiState.ITEMS -> {}
            InventoryUiState.EQUIPMENT -> {}
        }
    }

    private fun updateItemTableAndInfo(items: List<ItemModel>) {
        // update item table
        itemTable.clearEntries()
        items.forEach { item ->
            itemTable.item(item.name, item.amount)
        }
        itemTable.selectFirstEntry()
        itemTable.isVisible = true

        // update item info table
        items.firstOrNull()?.let { firstItem ->
            itemInfoTable.isVisible = true
            itemInfoTable.clearItem()
            itemInfoTable.item(firstItem.name, firstItem.description, firstItem.image)
        }
    }

    override fun onBackPressed() {
        if (state == InventoryUiState.EQUIPMENT || state == InventoryUiState.ITEMS) {
            itemTable.clearEntries()
            itemInfoTable.clearItem()
            itemTable.isVisible = false
            itemInfoTable.isVisible = false
            state = InventoryUiState.SELECT_OPTION
            viewModel.playSndMenuAbort()
            optionTable.resumeSelectAnimation()
            equipmentStatsTable.clearDiff()
            equipmentStatsTable.showEquipment(false)
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.inventoryView(
    model: InventoryViewModel,
    skin: Skin,
    init: (@Scene2dDsl InventoryView).(S) -> Unit = {},
): InventoryView = actor(InventoryView(model, skin), init)
