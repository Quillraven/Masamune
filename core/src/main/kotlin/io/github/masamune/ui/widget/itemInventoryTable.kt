package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.scene2d

@Scene2dDsl
class ItemInventoryTable(
    skin: Skin,
) : SelectionTable<InventoryItemWidget>(
    skin,
    entriesPerRow = 1,
), KGroup {

    fun item(title: String, amount: Int) {
        val shopItem = scene2d.inventoryItem(title, amount, skin) {
            select(this@ItemInventoryTable.numEntries == 0)
        }
        addEntry(shopItem)
    }

}

@Scene2dDsl
fun <S> KWidget<S>.itemInventoryTable(
    skin: Skin,
    init: (@Scene2dDsl ItemInventoryTable).(S) -> Unit = {},
): ItemInventoryTable = actor(ItemInventoryTable(skin), init)
