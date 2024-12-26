package io.github.masamune.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.masamune.ui.model.CombatFinishViewModel
import io.github.masamune.ui.model.I18NKey
import io.github.masamune.ui.model.UiCombatFinishState
import io.github.masamune.ui.widget.OptionTable
import io.github.masamune.ui.widget.optionTable
import ktx.actors.plusAssign
import ktx.actors.then
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import ktx.scene2d.label
import ktx.scene2d.table

@Scene2dDsl
class CombatFinishView(
    viewModel: CombatFinishViewModel,
    skin: Skin,
) : View<CombatFinishViewModel>(skin, viewModel), KTable {

    private val xpLabel: Label
    private val content: Label
    private val optionTable: OptionTable

    init {
        setFillParent(true)

        // nested inner table to center the content of the dialog automatically
        table(skin) {
            background = skin.getDrawable("dialog_frame")

            this@CombatFinishView.xpLabel = label("", defaultStyle, skin) {
                setAlignment(Align.topLeft)
                color = skin.getColor("dark_grey")
                it.grow().padBottom(5f).row()
            }
            this@CombatFinishView.content = label("", defaultStyle, skin) {
                setAlignment(Align.topLeft)
                color = skin.getColor("dark_grey")
                it.grow().padBottom(10f).row()
            }
            this@CombatFinishView.optionTable = optionTable(skin) {
                it.fill().align(Align.left)
            }
        }

        registerOnPropertyChanges()
    }

    private fun registerOnPropertyChanges() {
        viewModel.onPropertyChange(CombatFinishViewModel::xpToGain) { xp ->
            xpLabel.txt = buildString {
                append(i18nTxt(I18NKey.COMBAT_TOTAL_XP))
                append(": ")
                append(xp)
            }
        }
        viewModel.onPropertyChange(CombatFinishViewModel::combatSummary) { combatSummary ->
            content.txt = buildString {
                append(i18nTxt(I18NKey.COMBAT_DEFEATED_ENEMIES))
                appendLine(":")
                append(combatSummary.map { "${it.key}: ${it.value}x" }.joinToString("\n"))
            }
        }
        viewModel.onPropertyChange(CombatFinishViewModel::state) { state ->
            if (state == UiCombatFinishState.UNDEFINED) {
                return@onPropertyChange
            }

            optionTable.clearOptions()
            if (state == UiCombatFinishState.DEFEAT) {
                xpLabel.txt = ""
                content.txt = i18nTxt(I18NKey.COMBAT_DEFEAT)
                optionTable.option(i18nTxt(I18NKey.GENERAL_YES))
                optionTable.option(i18nTxt(I18NKey.GENERAL_NO))
            } else {
                optionTable.option(i18nTxt(I18NKey.DIALOG_OPTION_OK))
            }

            isVisible = true
            this.clearActions()
            this += Actions.alpha(0.25f) then Actions.fadeIn(0.5f, Interpolation.bounceIn)
        }
    }

    override fun onSelectPressed() {
        if (optionTable.children.size == 1) {
            // victory summary -> end combat
            isVisible = false
            viewModel.quitCombat()
            return
        }

        // defeat options
        if (optionTable.selectedOption == 0) {
            viewModel.restartCombat()
        } else {
            viewModel.quitCombat()
        }
        isVisible = false
    }

    override fun onDownPressed() {
        optionTable.nextOption()
    }

    override fun onUpPressed() {
        optionTable.prevOption()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.combatFinishView(
    model: CombatFinishViewModel,
    skin: Skin,
    init: (@Scene2dDsl CombatFinishView).(S) -> Unit = {},
): CombatFinishView = actor(CombatFinishView(model, skin), init)
