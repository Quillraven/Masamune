package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.scene2d
import ktx.scene2d.table

@Scene2dDsl
class ItemCombatTable(private val skin: Skin) : ScrollPane(null, skin), KGroup {

    private val contentTable: Table
    var selectedItem: Int = 0
        private set

    init {
        fadeScrollBars = false
        setFlickScroll(false)
        setForceScroll(false, true)
        setOverscroll(false, false)
        setScrollingDisabled(true, false)

        contentTable = scene2d.table(skin).top().padTop(5f).padRight(10f)

        actor = contentTable
    }

    fun clearItems() {
        contentTable.clear()
        selectedItem = 0
    }

    fun item(title: String, targetDescriptor: String, amount: Int) {
        val itemEntry = scene2d.itemEntry(title, targetDescriptor, amount, skin) {
            select(!this@ItemCombatTable.contentTable.hasChildren())
        }
        val cell = contentTable.add(itemEntry)
        cell.growX()
        if (contentTable.children.size % ITEMS_PER_ROW == 0) {
            cell.row()
        } else {
            cell.padRight(25f)
        }
    }

    fun prevItem(step: Int = 1): Boolean {
        var result = false
        repeat(step) {
            val selResult = selectOption(selectedItem - 1)
            result = result or selResult
        }
        return result
    }

    fun nextItem(step: Int = 1): Boolean {
        var result = false
        repeat(step) {
            val selResult = selectOption(selectedItem + 1)
            result = result or selResult
        }
        return result
    }

    fun hasNoItem(): Boolean = !contentTable.hasChildren()

    private fun selectOption(idx: Int): Boolean {
        if (hasNoItem()) {
            return false
        }

        val realIdx = when {
            idx < 0 -> 0
            idx >= contentTable.children.size -> contentTable.children.size - 1
            else -> idx
        }

        if (realIdx == selectedItem) {
            return false
        }

        (contentTable.getChild(selectedItem) as ItemEntryWidget).select(false)
        selectedItem = realIdx
        (contentTable.getChild(selectedItem) as ItemEntryWidget).select(true)
        return true
    }

    fun selectFirstItem() {
        selectOption(0)
    }

    companion object {
        const val ITEMS_PER_ROW = 2
    }

}

@Scene2dDsl
fun <S> KWidget<S>.itemCombatTable(
    skin: Skin,
    init: (@Scene2dDsl ItemCombatTable).(S) -> Unit = {},
): ItemCombatTable = actor(ItemCombatTable(skin), init)
