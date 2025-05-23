package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.actions.Actions.forever
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Scaling
import ktx.actors.plusAssign
import ktx.actors.then
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import ktx.scene2d.image
import ktx.scene2d.label

@Scene2dDsl
class CombatItemWidget(
    title: String,
    targetDescriptor: String,
    amount: Int,
    skin: Skin,
) : Table(skin), KTable, SelectableWidget {

    private val image: Image

    init {
        image = image(skin.getDrawable("arrow")) { cell ->
            setScaling(Scaling.contain)
            this += forever(fadeOut(0.5f) then fadeIn(0.25f) then delay(0.25f))
            cell.padRight(5f).padLeft(5f)
        }

        // remove target descriptor for now since it looks weird
        // label("$title [#${skin.getColor("red")}]${targetDescriptor.first()}[] ${amount}x", defaultStyle, skin) {
        label("$title ${amount}x", defaultStyle, skin) {
            it.left().expandX()
        }
    }

    override fun select(value: Boolean) {
        image.isVisible = value
    }

}

@Scene2dDsl
fun <S> KWidget<S>.combatItem(
    title: String,
    targetDescriptor: String,
    amount: Int,
    skin: Skin,
    init: (@Scene2dDsl CombatItemWidget).(S) -> Unit = {},
): CombatItemWidget = actor(CombatItemWidget(title, targetDescriptor, amount, skin), init)
