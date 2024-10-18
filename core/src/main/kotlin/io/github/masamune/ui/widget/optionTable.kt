package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import ktx.scene2d.scene2d

@Scene2dDsl
class OptionTable(skin: Skin) : Table(skin), KTable {

    var selectedOption: Int = 0
        private set

    init {
        align(Align.left)
    }

    fun clearOptions() {
        clear()
        selectedOption = 0
    }

    fun option(text: String, optionStyle: String = defaultStyle) {
        val option = scene2d.dialogOption(text, skin, optionStyle) {
            select(!this@OptionTable.hasChildren())
        }
        add(option).uniformX().left().fillX().padBottom(5f).row()
    }

    fun prevOption(): Boolean = selectOption(selectedOption - 1)

    fun nextOption(): Boolean = selectOption(selectedOption + 1)

    private fun selectOption(idx: Int): Boolean {
        val realIdx = when {
            idx < 0 -> (children.size) - 1
            idx >= (children.size) -> 0
            else -> idx
        }

        if (realIdx == selectedOption) {
            return false
        }

        (getChild(selectedOption) as DialogOptionWidget).select(false)
        selectedOption = realIdx
        (getChild(selectedOption) as DialogOptionWidget).select(true)
        return true
    }

}

@Scene2dDsl
fun <S> KWidget<S>.optionTable(
    skin: Skin,
    init: (@Scene2dDsl OptionTable).(S) -> Unit = {},
): OptionTable = actor(OptionTable(skin), init)
