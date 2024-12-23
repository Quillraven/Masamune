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
class MagicTable(private val skin: Skin) : ScrollPane(null, skin), KGroup {

    private val contentTable: Table
    var selectedMagic: Int = 0
        private set

    init {
        fadeScrollBars = false
        setFlickScroll(false)
        setForceScroll(false, true)
        setOverscroll(false, false)
        setScrollingDisabled(true, false)

        contentTable = scene2d.table(skin).top().padTop(5f)

        actor = contentTable
    }

    fun clearMagic() {
        contentTable.clear()
        selectedMagic = 0
    }

    fun magic(title: String, targetDescriptor: String, mana: Int) {
        val magicEntry = scene2d.magicEntry(title, targetDescriptor, mana, skin) {
            select(!this@MagicTable.contentTable.hasChildren())
        }
        val cell = contentTable.add(magicEntry)
        cell.growX()
        if (contentTable.children.size % 3 == 0) {
            cell.row()
        } else {
            cell.padRight(25f)
        }
    }

    fun prevMagic(): Boolean = selectOption(selectedMagic - 1)

    fun nextMagic(): Boolean = selectOption(selectedMagic + 1)

    fun hasNoMagic(): Boolean = !contentTable.hasChildren()

    private fun selectOption(idx: Int): Boolean {
        if (hasNoMagic()) {
            return false
        }

        val realIdx = when {
            idx < 0 -> 0
            idx >= contentTable.children.size -> contentTable.children.size - 1
            else -> idx
        }

        if (realIdx == selectedMagic) {
            return false
        }

        (contentTable.getChild(selectedMagic) as MagicEntryWidget).select(false)
        selectedMagic = realIdx
        (contentTable.getChild(selectedMagic) as MagicEntryWidget).select(true)
        return true
    }

}

@Scene2dDsl
fun <S> KWidget<S>.magicTable(
    skin: Skin,
    init: (@Scene2dDsl MagicTable).(S) -> Unit = {},
): MagicTable = actor(MagicTable(skin), init)
