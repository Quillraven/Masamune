package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.masamune.ui.model.StatsViewModel
import io.github.masamune.ui.model.UIStats
import io.github.masamune.ui.widget.MenuItemLabel
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
    private val talonsMenuItemLabel: MenuItemLabel

    private val levelMenuItemLabel: MenuItemLabel
    private val xpMenuItemLabel: MenuItemLabel
    private val xpProgressBar: ProgressBar

    private val strengthMenuItemLabel: MenuItemLabel
    private val agilityMenuItemLabel: MenuItemLabel
    private val constitutionMenuItemLabel: MenuItemLabel
    private val intelligenceMenuItemLabel: MenuItemLabel

    private val attackMenuItemLabel: MenuItemLabel
    private val armorMenuItemLabel: MenuItemLabel
    private val resistanceMenuItemLabel: MenuItemLabel

    private val physicalEvadeMenuItemLabel: MenuItemLabel
    private val magicalEvadeMenuItemLabel: MenuItemLabel
    private val criticalStrikeMenuItemLabel: MenuItemLabel
    private val arcaneStrikeMenuItemLabel: MenuItemLabel

    private val lifeMenuItemLabel: MenuItemLabel
    private val lifeProgressBar: ProgressBar
    private val manaMenuItemLabel: MenuItemLabel
    private val manaProgressBar: ProgressBar

    init {
        background = skin.getDrawable("dialog_frame")
        setFillParent(true)

        table(skin) { leftTableCell ->
            align(Align.center)
            defaults().padBottom(MENU_ITEM_BOT_PADDING).fillX()

            // hero image and name
            frameImage(skin, "dialog_face_frame", "hero") { cell ->
                cell.minWidth(375.0f).row()
            }
            this@StatsView.nameLabel = label("", "dialog_image_caption", skin) { cell ->
                setAlignment(Align.center)
                cell.padBottom(CATEGORY_BOT_PADDING).row()
            }

            // Level + XP
            this@StatsView.levelMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.xpMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.xpProgressBar = progressBar(style = "yellow", skin = skin) { cell ->
                cell.padBottom(CATEGORY_BOT_PADDING).row()
            }

            // Hit points
            this@StatsView.lifeMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.lifeProgressBar = progressBar(style = "green", skin = skin) { cell ->
                cell.padBottom(2 * MENU_ITEM_BOT_PADDING).row()
            }

            // Mana points
            this@StatsView.manaMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.manaProgressBar = progressBar(style = "blue", skin = skin) { cell ->
                cell.padBottom(CATEGORY_BOT_PADDING).row()
            }

            // Money
            this@StatsView.talonsMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }

            leftTableCell.fillX().growY().pad(EDGE_PADDING, EDGE_PADDING, EDGE_PADDING, 75f)
        }

        table(skin) { rightTableCell ->
            align(Align.center)
            defaults().padBottom(MENU_ITEM_BOT_PADDING).growX()

            // Attributes
            this@StatsView.strengthMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.agilityMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.constitutionMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.intelligenceMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.padBottom(CATEGORY_BOT_PADDING).row()
            }

            // Attack, Armor, Resistance
            this@StatsView.attackMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.armorMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.resistanceMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.padBottom(CATEGORY_BOT_PADDING).row()
            }

            // Percentage stats (phys. + magical evasion, phys. + magical critical strike)
            this@StatsView.physicalEvadeMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.magicalEvadeMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.criticalStrikeMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }
            this@StatsView.arcaneStrikeMenuItemLabel = menuItemLabel(skin, "", "") { cell ->
                cell.row()
            }

            rightTableCell.grow().pad(EDGE_PADDING, 0f, EDGE_PADDING, EDGE_PADDING)
        }

        registerOnPropertyChanges(viewModel)
    }

    private fun registerOnPropertyChanges(model: StatsViewModel) {
        model.onPropertyChange(StatsViewModel::playerName) { name ->
            isVisible = name.isNotBlank()
            nameLabel.txt = name
        }
        model.onPropertyChange(StatsViewModel::playerStats) { labelsAndStats ->
            val errorTitleLabel = "???" to "0"

            talonsMenuItemLabel.txt(labelsAndStats[UIStats.TALONS] ?: errorTitleLabel)

            levelMenuItemLabel.txt(labelsAndStats[UIStats.LEVEL] ?: errorTitleLabel)
            val (xpLabel, xpNeededValue) = labelsAndStats[UIStats.XP_NEEDED] ?: errorTitleLabel
            val xpCurrent = labelsAndStats[UIStats.XP]?.second ?: "1"
            val xpPercentage = xpCurrent.toFloat() / xpNeededValue.toFloat()
            xpMenuItemLabel.txt(xpLabel, xpNeededValue)
            xpProgressBar.value = xpPercentage.coerceIn(0f, 1f)

            strengthMenuItemLabel.txt(labelsAndStats[UIStats.STRENGTH] ?: errorTitleLabel)
            agilityMenuItemLabel.txt(labelsAndStats[UIStats.AGILITY] ?: errorTitleLabel)
            intelligenceMenuItemLabel.txt(labelsAndStats[UIStats.INTELLIGENCE] ?: errorTitleLabel)
            constitutionMenuItemLabel.txt(labelsAndStats[UIStats.CONSTITUTION] ?: errorTitleLabel)

            attackMenuItemLabel.txt(labelsAndStats[UIStats.ATTACK] ?: errorTitleLabel)
            armorMenuItemLabel.txt(labelsAndStats[UIStats.ARMOR] ?: errorTitleLabel)
            resistanceMenuItemLabel.txt(labelsAndStats[UIStats.RESISTANCE] ?: errorTitleLabel)

            physicalEvadeMenuItemLabel.txt(labelsAndStats[UIStats.PHYSICAL_EVADE] ?: errorTitleLabel)
            magicalEvadeMenuItemLabel.txt(labelsAndStats[UIStats.MAGICAL_EVADE] ?: errorTitleLabel)
            criticalStrikeMenuItemLabel.txt(labelsAndStats[UIStats.CRITICAL_STRIKE] ?: errorTitleLabel)
            arcaneStrikeMenuItemLabel.txt(labelsAndStats[UIStats.ARCANE_STRIKE] ?: errorTitleLabel)

            val (lifeLabel, lifeValue) = labelsAndStats[UIStats.LIFE] ?: errorTitleLabel
            val lifeMaxValue = labelsAndStats[UIStats.LIFE_MAX]?.second ?: "1"
            val lifePercentage = lifeValue.toFloat() / lifeMaxValue.toFloat()
            lifeMenuItemLabel.txt(lifeLabel, "$lifeValue/$lifeMaxValue")
            lifeProgressBar.value = lifePercentage.coerceIn(0f, 1f)

            val (manaLabel, manaValue) = labelsAndStats[UIStats.MANA] ?: errorTitleLabel
            val manaMaxValue = labelsAndStats[UIStats.MANA_MAX]?.second ?: "1"
            val manaPercentage = manaValue.toFloat() / manaMaxValue.toFloat()
            manaMenuItemLabel.txt(manaLabel, "$manaValue/$manaMaxValue")
            manaProgressBar.value = manaPercentage.coerceIn(0f, 1f)
        }
    }

    override fun onBackPressed() {
        viewModel.triggerClose()
    }

    companion object {
        private const val EDGE_PADDING = 25f
        private const val CATEGORY_BOT_PADDING = 40f
        private const val MENU_ITEM_BOT_PADDING = 10f
    }
}

@Scene2dDsl
fun <S> KWidget<S>.statsView(
    model: StatsViewModel,
    skin: Skin,
    init: (@Scene2dDsl StatsView).(S) -> Unit = {},
): StatsView = actor(StatsView(model, skin), init)
