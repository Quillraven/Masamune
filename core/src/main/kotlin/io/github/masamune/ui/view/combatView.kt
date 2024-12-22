package io.github.masamune.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.masamune.ui.model.CombatViewModel
import ktx.actors.txt
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import ktx.scene2d.label
import ktx.scene2d.progressBar
import ktx.scene2d.table

@Scene2dDsl
class CombatView(
    model: CombatViewModel,
    skin: Skin,
) : View<CombatViewModel>(skin, model), KGroup {

    private val playerInfoTable: Table
    private val playerNameLabel: Label
    private val playerLifeLabel: Label
    private val lifeProgressBar: ProgressBar
    private val playerManaLabel: Label
    private val manaProgressBar: ProgressBar

    init {
        playerInfoTable = table(skin) {
            background = skin.getDrawable("nine_path_bg_2")
            pad(10f)

            this@CombatView.playerNameLabel = label("", defaultStyle, skin) {
                this.setAlignment(Align.left)
                it.padBottom(10f).row()
            }

            table(skin) {
                this@CombatView.lifeProgressBar = progressBar(0f, 1f, 0.01f, false, "green", skin) {
                    this.value = 1f
                    this.setAnimateDuration(1f)
                    this.setAnimateInterpolation(Interpolation.pow4Out)
                    it.padRight(15f)
                }
                this@CombatView.playerLifeLabel = label("", defaultStyle, skin)

                it.growX().row()
            }

            table(skin) {
                this@CombatView.manaProgressBar = progressBar(0f, 1f, 0.01f, false, "blue", skin) {
                    this.value = 1f
                    this.setAnimateDuration(1f)
                    this.setAnimateInterpolation(Interpolation.pow4Out)
                    it.padRight(15f)
                }
                this@CombatView.playerManaLabel = label("", defaultStyle, skin)

                it.growX().row()
            }

            pack()
        }

        registerOnPropertyChanges(model)
    }

    private fun registerOnPropertyChanges(model: CombatViewModel) {
        model.onPropertyChange(CombatViewModel::playerName) {
            playerNameLabel.txt = it
            playerInfoTable.pack()
        }
        model.onPropertyChange(CombatViewModel::playerLife) { (current, max) ->
            playerLifeLabel.txt = "${current.coerceAtLeast(0f).toInt()}/${max.toInt()}"
            lifeProgressBar.value = (current / max).coerceIn(0f, 1f)
            playerInfoTable.pack()
        }
        model.onPropertyChange(CombatViewModel::playerMana) { (current, max) ->
            playerManaLabel.txt = "${current.coerceAtLeast(0f).toInt()}/${max.toInt()}"
            manaProgressBar.value = (current / max).coerceIn(0f, 1f)
            playerInfoTable.pack()
        }
        model.onPropertyChange(CombatViewModel::playerPosition) { position ->
            playerInfoTable.setPosition(position.x - playerInfoTable.width, position.y)
        }
    }

}

@Scene2dDsl
fun <S> KWidget<S>.combatView(
    model: CombatViewModel,
    skin: Skin,
    init: (@Scene2dDsl CombatView).(S) -> Unit = {},
): CombatView = actor(CombatView(model, skin), init)
