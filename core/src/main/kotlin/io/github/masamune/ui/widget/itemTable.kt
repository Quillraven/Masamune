package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.scene2d

@Scene2dDsl
class ItemTable(skin: Skin) : ScrollPane(null, skin), KGroup {

    private val contentTable: TypedTable<ShopItemWidget>
    var selectedItem: Int = 0
        private set

    init {
        fadeScrollBars = false
        setFlickScroll(false)
        setForceScroll(false, true)
        setOverscroll(false, false)
        setScrollingDisabled(true, false)

        contentTable = scene2d.typedTable(skin)
        contentTable.top().padTop(5f)
        actor = contentTable
    }

    fun clearItems() {
        contentTable.clear()
        selectedItem = 0
    }

    fun clearAmounts() {
        contentTable.forEach { it.amount(0) }
    }

    fun item(title: String, cost: Int) {
        val shopItem = scene2d.shopItem(title, cost, contentTable.skin) {
            select(!this@ItemTable.contentTable.hasChildren())
        }
        contentTable.add(shopItem).growX().row()
    }

    fun amount(value: Int) {
        contentTable[selectedItem].amount(value)
    }

    fun prevItem(): Boolean = selectOption(selectedItem - 1)

    fun nextItem(): Boolean = selectOption(selectedItem + 1)

    fun hasNoItems(): Boolean = !contentTable.hasChildren()

    private fun selectOption(idx: Int): Boolean {
        if (hasNoItems()) {
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

        contentTable[selectedItem].select(false)
        selectedItem = realIdx
        contentTable[selectedItem].select(true)
        return true
    }

    fun stopSelectAnimation() {
        contentTable[selectedItem].stopSelectAnimation()
    }

    fun resumeSelectAnimation() {
        contentTable[selectedItem].resumeSelectAnimation()
    }

}

@Scene2dDsl
fun <S> KWidget<S>.itemTable(
    skin: Skin,
    init: (@Scene2dDsl ItemTable).(S) -> Unit = {},
): ItemTable = actor(ItemTable(skin), init)
