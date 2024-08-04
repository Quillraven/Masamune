package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.actors.plusAssign
import ktx.scene2d.*

data class ProgressBarStyle(
    val background: Drawable = BaseDrawable(),
    val progress: Drawable = BaseDrawable(),
)

@Scene2dDsl
class ProgressBar(
    skin: Skin,
    styleName: String,
) : WidgetGroup(), KGroup {

    private val style = skin[styleName, ProgressBarStyle::class.java]
    private val background: Image = Image(style.background)
    private val progress: Image = Image(style.progress)
    private val minProgressWidth = progress.width
    var value: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateProgressSize(value)
        }

    init {
        this += background
        this += progress
    }

    override fun sizeChanged() {
        super.sizeChanged()
        background.setSize(width, height)
        updateProgressSize(value)
    }

    private fun updateProgressSize(value: Float) {
        val newWidth = width * value
        progress.setSize(newWidth, height)
        if (newWidth < minProgressWidth) {
            // to avoid weird 9-patch rendering if the width is smaller than the minimum size of the 9-patch,
            // we will simply not draw it at all
            progress.setScale(0f, 1f)
        } else {
            progress.setScale(1f, 1f)
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.progressBar(
    skin: Skin,
    style: String = defaultStyle,
    init: (@Scene2dDsl ProgressBar).(S) -> Unit = {},
): ProgressBar = actor(ProgressBar(skin, style), init)
