package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.table

@Scene2dDsl
class ItemInfoTable(skin: Skin) : Table(skin), KTable {

    private val image: Image
    private val name: Label
    private val description: Label

    init {
        background = skin.getDrawable("dialog_frame")

        table(skin) { headerTblCell ->
            this@ItemInfoTable.image = image()
            this@ItemInfoTable.name = label("", "dialog_content", skin) { lblCell ->
                setAlignment(Align.center)
                color = skin.getColor("highlight_blue")
                lblCell.growX()
            }
            headerTblCell.growX().row()
        }

        description = label("", "dialog_content", skin) { lblCell ->
            wrap = true
            color = skin.getColor("dark_grey")
            setAlignment(Align.topLeft)
            lblCell.padTop(10f).grow()
        }
    }

    fun item(name: String, description: String, drawable: Drawable?) {
        this.name.txt = name
        this.description.txt = description
        this.image.drawable = drawable
    }

    fun clearItem() {
        this.name.txt = ""
        this.description.txt = ""
        this.image.drawable = null
    }
}

@Scene2dDsl
fun <S> KWidget<S>.itemInfoTable(
    skin: Skin,
    init: (@Scene2dDsl ItemInfoTable).(S) -> Unit = {},
): ItemInfoTable = actor(ItemInfoTable(skin), init)
