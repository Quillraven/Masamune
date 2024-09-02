package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label

class MenuItemLabel(
    skin: Skin,
    title: String,
    value: String,
) : KTable, Table(skin) {

    init {
        label(title, "dialog_content", skin) { cell ->
            this.color = skin.getColor("dark_grey")
            cell.growX()
        }
        label(value, "dialog_content", skin) { cell ->
            this.color = skin.getColor("dark_grey")
            this.setAlignment(Align.right)
            cell.growX()
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.menuItemLabel(
    skin: Skin,
    title: String,
    value: String,
    init: (@Scene2dDsl MenuItemLabel).(S) -> Unit = {},
): MenuItemLabel = actor(MenuItemLabel(skin, title, value), init)
