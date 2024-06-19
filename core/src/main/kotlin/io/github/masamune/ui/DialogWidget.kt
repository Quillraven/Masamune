package io.github.masamune.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.rafaskoberg.gdx.typinglabel.TypingLabel
import ktx.actors.centerPosition
import ktx.actors.txt

data class DialogWidgetStyle(
    val background: Drawable? = null,
    val imageFrame: Drawable? = null,
    val imageCaptionStyle: String? = null,
    val contentStyle: String = "default",
    val optionStyle: String = "default",
)

class DialogWidget(
    private val skin: Skin,
    styleName: String = "default"
) : Container<Actor>() {

    private val style: DialogWidgetStyle = skin[styleName, DialogWidgetStyle::class.java]
    private val image: Image
    private val imageCaption: Label
    private val contentLabel: TypingLabel
    private val optionTable: Table
    var selectedOption: DialogOptionWidget
        private set

    init {
        // image + caption
        val imgTable = initImageAndCaption(skin, style)
        image = imgTable.findActor("image")
        imageCaption = imgTable.findActor("caption")

        // main text label
        contentLabel = TypingLabel("", skin, style.contentStyle).apply {
            wrap = true
            setAlignment(Align.topLeft)
        }

        // options
        optionTable = Table(skin)
        optionTable.defaults().pad(1f)
        selectedOption = DialogOptionWidget("", skin, style.optionStyle)
        optionTable.add(selectedOption).row()

        // add everything into a single table
        actor = Table(skin).apply {
            background = style.background
            add(imgTable).top().minWidth(110f)
            add(contentLabel).expand().fill().padLeft(20f).row()
            add()
            add(optionTable).left().padLeft(20f).padTop(10f)
        }

        minSize(542f, 200f)
        maxWidth(542f)
        pack()
    }

    private fun initImageAndCaption(skin: Skin, style: DialogWidgetStyle): Table {
        val frame = Image(style.imageFrame)
        val img = Image(null as Drawable?).apply {
            // Image must be smaller than the frame to see the frame.
            // Otherwise, the image will simply overlap the entire frame.
            setScaling(Scaling.contain)
            name = "image"
        }
        val stack = Stack(frame, img)
        val imgCaption = Label("", skin, style.imageCaptionStyle).apply {
            wrap = true
            setAlignment(Align.center)
            name = "caption"
        }

        return Table(skin).apply {
            add(stack).row()
            add(imgCaption).expand().fill().padTop(5f)
        }
    }

    fun option(text: String) {
        val firstOption = (optionTable.getChild(0) as DialogOptionWidget)
        if (firstOption.text.isBlank()) {
            // first option has no text yet -> use it
            firstOption.text = " $text "
            pack()
            return
        }

        // first option already provided -> add a new one
        val newOption = DialogOptionWidget(text, skin, style.optionStyle)
        newOption.select(false)
        optionTable.add(newOption).row()
        pack()
    }

    fun content(text: String) {
        contentLabel.txt = text
        // important to call pack, otherwise the background graphic is not properly resized for whatever reason
        pack()
    }

    fun image(drawable: Drawable, caption: String? = null) {
        image.drawable = drawable
        caption?.let { imageCaption.txt = it }
    }

    fun prevOption() {
        optionTable.children.forEachIndexed { idx, dialogOption ->
            if (dialogOption === selectedOption) {
                selectOption(idx - 1)
                return
            }
        }
    }

    fun nextOption() {
        optionTable.children.forEachIndexed { idx, dialogOption ->
            if (dialogOption === selectedOption) {
                selectOption(idx + 1)
                return
            }
        }
    }

    private fun selectOption(idx: Int) {
        val realIdx = when {
            idx < 0 -> optionTable.children.size - 1
            idx >= optionTable.children.size -> 0
            else -> idx
        }

        selectedOption.select(false)
        selectedOption = optionTable.getChild(realIdx) as DialogOptionWidget
        selectedOption.select(true)
    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        if (stage != null) {
            // automatically center dialog widget when added to a stage
            centerPosition()
        }
    }

}
