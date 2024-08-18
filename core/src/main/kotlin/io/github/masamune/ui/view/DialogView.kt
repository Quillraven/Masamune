package io.github.masamune.ui.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.rafaskoberg.gdx.typinglabel.TypingLabel
import io.github.masamune.ui.model.DialogUiContent
import io.github.masamune.ui.model.DialogViewModel
import io.github.masamune.ui.widget.DialogOptionWidget
import io.github.masamune.ui.widget.dialogOption
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.stack
import ktx.scene2d.table
import ktx.scene2d.verticalGroup

data class DialogViewStyle(
    val background: Drawable = BaseDrawable(),
    val imageFrame: Drawable = BaseDrawable(),
    val imageCaptionStyle: String = defaultStyle,
    val contentStyle: String = defaultStyle,
    val optionStyle: String = defaultStyle,
)

/**
 * DialogView is a 2x2 table where:
 * - top left is a vertical group with an image stack (frame+image) and an image caption
 * - top right is the main content which is a typing label
 * - bottom left is an empty cell
 * - bottom right is another table with a select image and an option text per row
 *
 * ```
 * +-------------+----------------+
 * | IMAGE       | CONTENT LABEL  |
 * |             |                |
 * | CAPTION_LBL |                |
 * +-------------+----------------+
 * |             | OPTION 1       |
 * |             | OPTION 2       |
 * +-------------+----------------+
 * ```
 */
@Scene2dDsl
class DialogView(
    private val model: DialogViewModel,
    skin: Skin,
    styleName: String = defaultStyle,
) : Table(skin), KTable, View {

    private val style = skin[styleName, DialogViewStyle::class.java]

    private val imageGroup: VerticalGroup
    private val image: Image
    private val imageCaption: Label

    private val content: TypingLabel

    private val optionsTable: Table
    private var selectedOption: Int = 0

    init {
        setFillParent(true)

        // nested inner table to center the content of the dialog automatically
        table(skin) {
            background = this@DialogView.style.background

            // first cell = vertical group for an image and image caption
            this@DialogView.imageGroup = verticalGroup { vGroupCell ->
                align(Align.topLeft)
                expand().fill()

                // image is a stack of an image frame + the image itself
                stack {
                    image(this@DialogView.style.imageFrame) {
                        setScaling(Scaling.contain)
                    }
                    this@DialogView.image = image(BaseDrawable()) {
                        setScaling(Scaling.contain)
                    }
                }

                this@DialogView.imageCaption = label("", this@DialogView.style.imageCaptionStyle, skin) {
                    setAlignment(Align.top, Align.center)
                    wrap = false
                }

                vGroupCell.padLeft(5f).padRight(15f).fill().minWidth(MIN_IMAGE_WIDTH)
            }

            // second cell is the main content label
            this@DialogView.content = typingLabel("", this@DialogView.style.contentStyle, skin) { contentCell ->
                val defaultTextColor = "#695454"
                defaultToken = "{COLOR=$defaultTextColor}"
                clearColor.set(Color.valueOf(defaultTextColor))

                setAlignment(Align.topLeft)
                wrap = true
                contentCell.grow().minWidth(MIN_CONTENT_WIDTH).minHeight(MIN_CONTENT_HEIGHT).padBottom(10f)
            }
            row()

            // third cell (=bottom left) is an empty cell
            add()
            // fourth cell is the option table
            this@DialogView.optionsTable = table(skin) { optionTableCell ->
                align(Align.left)

                optionTableCell.fill().align(Align.left)
            }
        }

        registerOnPropertyChanges(model)
    }

    private fun registerOnPropertyChanges(model: DialogViewModel) {
        model.onPropertyChange(DialogViewModel::content) { dialogContent ->
            if (dialogContent == DialogUiContent.EMPTY_CONTENT) {
                isVisible = false
                return@onPropertyChange
            }

            isVisible = true
            val (txt, options, image, caption) = dialogContent
            text(txt)
            image(image, caption)
            clearOptions()
            options.forEach { option(it) }
        }
    }

    fun text(txt: String) {
        content.restart("{SLOW}{FADE}$txt")
    }

    fun image(drawableName: String?, caption: String? = null) {
        if (drawableName.isNullOrBlank()) {
            imageGroup.isVisible = false
            imageGroup.inCell.width(0f)
        } else {
            imageGroup.isVisible = true
            image.drawable = skin.getDrawable(drawableName)
            imageCaption.txt = caption?.replace(' ', '\n') ?: ""
            imageGroup.inCell.minWidth(MIN_IMAGE_WIDTH)
        }
    }

    fun clearOptions() {
        optionsTable.clear()
        selectedOption = 0
    }

    fun option(text: String) {
        val option = scene2d.dialogOption(text, skin, style.optionStyle) {
            select(!this@DialogView.optionsTable.hasChildren())
        }
        optionsTable.add(option).uniformX().left().fillX().padBottom(5f).row()
    }

    fun prevOption() = selectOption(selectedOption - 1)

    fun nextOption() = selectOption(selectedOption + 1)

    private fun selectOption(idx: Int) {
        val realIdx = when {
            idx < 0 -> (optionsTable.children.size) - 1
            idx >= (optionsTable.children.size) -> 0
            else -> idx
        }

        (optionsTable.getChild(selectedOption) as DialogOptionWidget).select(false)
        selectedOption = realIdx
        (optionsTable.getChild(selectedOption) as DialogOptionWidget).select(true)
    }

    override fun onUpPressed() {
        nextOption()
    }

    override fun onDownPressed() {
        prevOption()
    }

    override fun onSelectPressed() {
        if (!content.hasEnded()) {
            content.skipToTheEnd()
        } else {
            model.triggerOption(selectedOption)
        }
    }

    companion object {
        private const val MIN_IMAGE_WIDTH = 115f
        private const val MIN_CONTENT_WIDTH = 482f
        private const val MIN_CONTENT_HEIGHT = 120f
    }
}

@Scene2dDsl
fun <S> KWidget<S>.dialogView(
    model: DialogViewModel,
    skin: Skin,
    style: String = defaultStyle,
    init: (@Scene2dDsl DialogView).(S) -> Unit = {},
): DialogView = actor(DialogView(model, skin, style), init)
