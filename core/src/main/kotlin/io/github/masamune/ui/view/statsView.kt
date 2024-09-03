package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.masamune.ui.model.StatsViewModel
import io.github.masamune.ui.widget.frameImage
import io.github.masamune.ui.widget.menuItemLabel
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.progressBar
import ktx.scene2d.table


@Scene2dDsl
class StatsView(
    model: StatsViewModel,
    skin: Skin,
) : View<StatsViewModel>(skin, model), KTable {

    private val nameLabel: Label

    init {
        background = skin.getDrawable("dialog_frame")
        setFillParent(true)

        table(skin) { leftTableCell ->
            align(Align.center)
            defaults().padBottom(10f).fillX()

            // hero image and name
            frameImage(skin, "dialog_face_frame", "hero") { cell ->
                cell.minWidth(375.0f).row()
            }
            this@StatsView.nameLabel = label("", "dialog_image_caption", skin) { cell ->
                setAlignment(Align.center)
                cell.padBottom(35f).row()
            }

            // Level + XP
            menuItemLabel(skin, "Level", "2") { cell ->
                cell.row()
            }
            menuItemLabel(skin, "Next Level in:", "500") { cell ->
                cell.row()
            }
            progressBar(0f, 1f, 0.01f, vertical = false, "yellow", skin) { cell ->
                value = 0.5f
                cell.padBottom(30f).row()
            }

            // Hit points
            menuItemLabel(skin, "HP", "80/112") { cell ->
                cell.row()
            }
            progressBar(0f, 1f, 0.01f, vertical = false, "green", skin) { cell ->
                value = 0.5f
                cell.padBottom(20f).row()
            }

            // Mana points
            menuItemLabel(skin, "Mana", "12/20") { cell ->
                cell.row()
            }
            progressBar(0f, 1f, 0.01f, vertical = false, "blue", skin) { cell ->
                value = 0.5f
                cell.padBottom(20f).row()
            }

            // Money
            menuItemLabel(skin, "Talons", "312") { cell ->
                cell.growY().row()
            }

            leftTableCell.fillX().growY().pad(35f, 35f, 35f, 75f)
        }

        table(skin) { rightTableCell ->
            align(Align.center)
            defaults().padBottom(10f).growX()

            // Attributes
            menuItemLabel(skin, "Strength", "14") { cell ->
                cell.row()
            }
            menuItemLabel(skin, "Agility", "14") { cell ->
                cell.row()
            }
            menuItemLabel(skin, "Constitution", "14") { cell ->
                cell.row()
            }
            menuItemLabel(skin, "Intelligence", "14") { cell ->
                cell.padBottom(40f).row()
            }

            // Attack, Armor, Resistance
            menuItemLabel(skin, "Attack", "14") { cell ->
                cell.row()
            }
            menuItemLabel(skin, "Armor", "14") { cell ->
                cell.row()
            }
            menuItemLabel(skin, "Resistance", "14") { cell ->
                cell.padBottom(40f).row()
            }

            // Percentage stats (phys. + magical evasion, phys. + magical critical strike)
            menuItemLabel(skin, "Evade", "10%") { cell ->
                cell.row()
            }
            menuItemLabel(skin, "Repel", "0%") { cell ->
                cell.row()
            }
            menuItemLabel(skin, "Critical Strike", "25%") { cell ->
                cell.row()
            }
            menuItemLabel(skin, "Arcane Strike", "0%") { cell ->
                cell.row()
            }

            rightTableCell.grow().pad(35f, 0f, 35f, 35f)
        }

        registerOnPropertyChanges(viewModel)
    }

    private fun registerOnPropertyChanges(model: StatsViewModel) {
        model.onPropertyChange(StatsViewModel::playerName) { name ->
            isVisible = name.isNotBlank()
            nameLabel.txt = name
        }
    }

    override fun onBackPressed() {
        viewModel.triggerClose()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.statsView(
    model: StatsViewModel,
    skin: Skin,
    init: (@Scene2dDsl StatsView).(S) -> Unit = {},
): StatsView = actor(StatsView(model, skin), init)
