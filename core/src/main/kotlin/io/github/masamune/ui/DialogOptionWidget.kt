package io.github.masamune.ui

import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.actors.plusAssign
import ktx.actors.then
import ktx.actors.txt

data class DialogOptionWidgetStyle(
    val labelStyle: String = "default",
    val selectImage: Drawable? = null,
)

class DialogOptionWidget(
    txt: String,
    skin: Skin,
    styleName: String,
) : Table(skin) {

    private val style = skin[styleName, DialogOptionWidgetStyle::class.java]
    private val label: Label
    private val image: Image = Image(style.selectImage)

    var text: String
        get() = label.txt
        set(value) {
            label.txt = value
        }

    init {
        add(image).padRight(5f)
        image += forever(fadeOut(0.5f) then fadeIn(0.25f) then delay(0.25f))
        label = Label(" $txt ", skin, style.labelStyle)
        add(label)
    }

    fun select(value: Boolean) {
        image.isVisible = value
    }

}
