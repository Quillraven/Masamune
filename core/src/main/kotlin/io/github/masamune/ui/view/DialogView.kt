package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import io.github.masamune.ui.model.DialogUiContent
import io.github.masamune.ui.model.DialogViewModel
import io.github.masamune.ui.widget.DialogWidget

class DialogView(
    model: DialogViewModel,
    skin: Skin,
    styleName: String = "default",
) : Table(skin) {

    private val dialogWidget: DialogWidget

    init {
        setFillParent(true)
        dialogWidget = DialogWidget(skin, styleName)
        add(dialogWidget)
        isVisible = false

        model.onPropertyChange(DialogViewModel::content) { dialogContent ->
            if (dialogContent == DialogUiContent.EMPTY_CONTENT) {
                isVisible = false
                return@onPropertyChange
            }

            isVisible = true
            val (txt, options, image, caption) = dialogContent
            dialogWidget.content(txt)
            if (image != null) {
                val drawable = skin.getDrawable(image)
                dialogWidget.image(drawable, caption)
            } else {
                dialogWidget.clearImage()
            }
            dialogWidget.clearOptions()
            options.forEach { dialogWidget.option(it) }
        }

        model.onPropertyChange(DialogViewModel::selectedOptionIdx) { idx ->
            dialogWidget.selectOption(idx)
        }
    }

}
