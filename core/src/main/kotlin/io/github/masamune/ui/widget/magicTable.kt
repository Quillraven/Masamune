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

        contentTable = scene2d.table(skin).top().padTop(5f).padRight(10f)

        actor = contentTable
    }

    fun clearMagic() {
        contentTable.clear()
        selectedMagic = 0
    }

    fun magic(title: String, targetDescriptor: String, mana: Int, canPerform: Boolean) {
        val magicEntry = scene2d.magicEntry(title, targetDescriptor, mana, canPerform, skin) {
            select(false)
        }
        val cell = contentTable.add(magicEntry)
        cell.growX()
        if (contentTable.children.size % MAGIC_PER_ROW == 0) {
            cell.row()
        } else {
            cell.padRight(25f)
        }
    }

    private fun nextMagicIdx(fromIdx: Int): Int {
        if (hasNoMagic()) {
            return fromIdx
        }

        for (i in fromIdx + 1 until contentTable.children.size) {
            val entry = contentTable.children[i] as MagicEntryWidget
            if (entry.canPerform) {
                return i
            }
        }
        // search in opposite direction because there is no performable magic afterward
        for (i in fromIdx - 1 downTo 0) {
            val entry = contentTable.children[i] as MagicEntryWidget
            if (entry.canPerform) {
                return i
            }
        }
        return fromIdx
    }

    private fun prevMagicIdx(fromIdx: Int): Int {
        if (hasNoMagic()) {
            return fromIdx
        }

        for (i in fromIdx - 1 downTo 0) {
            val entry = contentTable.children[i] as MagicEntryWidget
            if (entry.canPerform) {
                return i
            }
        }
        // search in opposite direction because there is no performable magic afterward
        for (i in fromIdx + 1 until contentTable.children.size) {
            val entry = contentTable.children[i] as MagicEntryWidget
            if (entry.canPerform) {
                return i
            }
        }
        return fromIdx
    }

    fun prevMagic(step: Int = 1): Boolean {
        var result = false
        repeat(step) {
            val selResult = selectOption(prevMagicIdx(selectedMagic))
            result = result or selResult
        }
        return result
    }

    fun nextMagic(step: Int = 1): Boolean {
        var result = false
        repeat(step) {
            val selResult = selectOption(nextMagicIdx(selectedMagic))
            result = result or selResult
        }
        return result
    }

    fun selectFirstMagic() {
        if (hasNoMagic()) {
            return
        }

        contentTable.children.forEach { (it as MagicEntryWidget).select(false) }
        val entry = contentTable.children.first { (it as MagicEntryWidget).canPerform }
        (entry as MagicEntryWidget).select(true)
        selectedMagic = contentTable.children.indexOf(entry, true)
    }

    fun hasNoMagic(): Boolean = contentTable.children.count { (it as MagicEntryWidget).canPerform } == 0

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

    companion object {
        const val MAGIC_PER_ROW = 2
    }

}

@Scene2dDsl
fun <S> KWidget<S>.magicTable(
    skin: Skin,
    init: (@Scene2dDsl MagicTable).(S) -> Unit = {},
): MagicTable = actor(MagicTable(skin), init)
