package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import ktx.actors.plusAssign
import ktx.actors.then
import ktx.scene2d.*

data class DialogOptionStyle(
    val labelStyle: String = defaultStyle,
    val selectImage: Drawable = BaseDrawable(),
)

@Scene2dDsl
class DialogOptionWidget(
    text: String,
    skin: Skin,
    styleName: String,
) : Table(skin), KTable {

    private val image: Image

    init {
        val style = skin[styleName, DialogOptionStyle::class.java]

        left()

        image = image(style.selectImage) { cell ->
            setScaling(Scaling.contain)
            this += forever(fadeOut(0.5f) then fadeIn(0.25f) then delay(0.25f))
            cell.padRight(5f)
        }
        label(" $text ", style.labelStyle, skin) { cell ->
            cell.growX().align(Align.left)
        }
    }

    fun select(value: Boolean) {
        image.isVisible = value
    }

}

@Scene2dDsl
fun <S> KWidget<S>.dialogOption(
    text: String,
    skin: Skin,
    style: String = defaultStyle,
    init: (@Scene2dDsl DialogOptionWidget).(S) -> Unit = {},
): DialogOptionWidget = actor(DialogOptionWidget(text, skin, style), init)
