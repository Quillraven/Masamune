package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.rafaskoberg.gdx.typinglabel.TypingLabel
import io.github.masamune.ui.model.CutSceneViewModel
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

class CutSceneView(
    skin: Skin,
    model: CutSceneViewModel,
) : View<CutSceneViewModel>(skin, model), KTable {

    private val topLabel: TypingLabel
    private val centerLabel: TypingLabel
    private val bottomLabel: TypingLabel

    init {
        setFillParent(true)

        this@CutSceneView.topLabel = typingLabel("", "default", skin) {
            this.defaultToken = "{SLOW}{FADE}"
            this.setAlignment(Align.topLeft)
            this.wrap = true
            it.grow().pad(20f).row()
        }

        this@CutSceneView.centerLabel = typingLabel("", "default", skin) {
            this.defaultToken = "{SLOW}{FADE}"
            this.setAlignment(Align.center)
            this.wrap = true
            it.grow().pad(20f).row()
        }

        this@CutSceneView.bottomLabel = typingLabel("", "default", skin) {
            this.defaultToken = "{SLOW}{FADE}"
            this.setAlignment(Align.bottomLeft)
            this.wrap = true
            it.grow().pad(20f).row()
        }

        registerOnPropertyChanges()
    }

    override fun registerOnPropertyChanges() {
        viewModel.onPropertyChange(CutSceneViewModel::textModel) { textModel ->
            stage.isDebugAll = true
            when (textModel.align) {
                Align.topLeft, Align.top, Align.topRight -> topLabel.run {
                    this.restart()
                    txt = textModel.text
                    align(textModel.align)
                }

                Align.bottomLeft, Align.bottom, Align.bottomRight -> bottomLabel.run {
                    this.restart()
                    txt = textModel.text
                    align(textModel.align)
                }

                else -> centerLabel.run {
                    this.restart()
                    txt = textModel.text
                    align(textModel.align)
                }
            }
        }
    }

}

@Scene2dDsl
fun <S> KWidget<S>.cutSceneView(
    model: CutSceneViewModel,
    skin: Skin,
    init: (@Scene2dDsl CutSceneView).(S) -> Unit = {},
): CutSceneView = actor(CutSceneView(skin, model), init)
