package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.masamune.ui.model.I18NKey
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
            this@StatsView.levelMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_LEVEL), "") { cell ->
                    cell.row()
                }
            this@StatsView.xpMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_XP_NEEDED), "") { cell ->
                    cell.row()
                }
            this@StatsView.xpProgressBar = progressBar(style = "yellow", skin = skin) { cell ->
                cell.padBottom(CATEGORY_BOT_PADDING).row()
            }

            // Hit points
            this@StatsView.lifeMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_LIFE), "") { cell ->
                    cell.row()
                }
            this@StatsView.lifeProgressBar = progressBar(style = "green", skin = skin) { cell ->
                cell.padBottom(2 * MENU_ITEM_BOT_PADDING).row()
            }

            // Mana points
            this@StatsView.manaMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_MANA), "") { cell ->
                    cell.row()
                }
            this@StatsView.manaProgressBar = progressBar(style = "blue", skin = skin) { cell ->
                cell.padBottom(CATEGORY_BOT_PADDING).row()
            }

            // Money
            this@StatsView.talonsMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_TALONS), "") { cell ->
                    cell.row()
                }

            leftTableCell.fillX().growY().pad(EDGE_PADDING, EDGE_PADDING, EDGE_PADDING, 75f)
        }

        table(skin) { rightTableCell ->
            align(Align.center)
            defaults().padBottom(MENU_ITEM_BOT_PADDING).growX()

            // Attributes
            this@StatsView.strengthMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_STRENGTH), "") { cell ->
                    cell.row()
                }
            this@StatsView.agilityMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_AGILITY), "") { cell ->
                    cell.row()
                }
            this@StatsView.constitutionMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_CONSTITUTION), "") { cell ->
                    cell.row()
                }
            this@StatsView.intelligenceMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_INTELLIGENCE), "") { cell ->
                    cell.padBottom(CATEGORY_BOT_PADDING).row()
                }

            // Attack, Armor, Resistance
            this@StatsView.attackMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_ATTACK), "") { cell ->
                    cell.row()
                }
            this@StatsView.armorMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_ARMOR), "") { cell ->
                    cell.row()
                }
            this@StatsView.resistanceMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_RESISTANCE), "") { cell ->
                    cell.padBottom(CATEGORY_BOT_PADDING).row()
                }

            // Percentage stats (phys. + magical evasion, phys. + magical critical strike)
            this@StatsView.physicalEvadeMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_PHYSICAL_EVADE), "") { cell ->
                    cell.row()
                }
            this@StatsView.magicalEvadeMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_MAGICAL_EVADE), "") { cell ->
                    cell.row()
                }
            this@StatsView.criticalStrikeMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_CRITICAL_STRIKE), "") { cell ->
                    cell.row()
                }
            this@StatsView.arcaneStrikeMenuItemLabel =
                menuItemLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_ARCANE_STRIKE), "") { cell ->
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
        model.onPropertyChange(StatsViewModel::playerStats) { stats ->
            val missingValue = "0"

            // money
            talonsMenuItemLabel.value(stats[UIStats.TALONS] ?: missingValue)

            // experience
            levelMenuItemLabel.value(stats[UIStats.LEVEL] ?: missingValue)
            val xpNeededValue = stats[UIStats.XP_NEEDED] ?: "1"
            val xpCurrent = stats[UIStats.XP] ?: missingValue
            val xpPercentage = xpCurrent.toFloat() / xpNeededValue.toFloat()
            xpMenuItemLabel.value("${(xpNeededValue.toInt() - xpCurrent.toInt()).coerceAtLeast(0)}")
            xpProgressBar.value = xpPercentage.coerceIn(0f, 1f)

            // stats
            strengthMenuItemLabel.value(stats[UIStats.STRENGTH] ?: missingValue)
            agilityMenuItemLabel.value(stats[UIStats.AGILITY] ?: missingValue)
            constitutionMenuItemLabel.value(stats[UIStats.CONSTITUTION] ?: missingValue)
            intelligenceMenuItemLabel.value(stats[UIStats.INTELLIGENCE] ?: missingValue)

            attackMenuItemLabel.value(stats[UIStats.ATTACK] ?: missingValue)
            armorMenuItemLabel.value(stats[UIStats.ARMOR] ?: missingValue)
            resistanceMenuItemLabel.value(stats[UIStats.RESISTANCE] ?: missingValue)

            physicalEvadeMenuItemLabel.value(stats[UIStats.PHYSICAL_EVADE] ?: missingValue)
            magicalEvadeMenuItemLabel.value(stats[UIStats.MAGICAL_EVADE] ?: missingValue)
            criticalStrikeMenuItemLabel.value(stats[UIStats.CRITICAL_STRIKE] ?: missingValue)
            arcaneStrikeMenuItemLabel.value(stats[UIStats.ARCANE_STRIKE] ?: missingValue)

            // life
            val lifeValue = stats[UIStats.LIFE] ?: missingValue
            val lifeMaxValue = stats[UIStats.LIFE_MAX] ?: "1"
            val lifePercentage = lifeValue.toFloat() / lifeMaxValue.toFloat()
            lifeMenuItemLabel.value("$lifeValue/$lifeMaxValue")
            lifeProgressBar.value = lifePercentage.coerceIn(0f, 1f)

            // mana
            val manaValue = stats[UIStats.MANA] ?: missingValue
            val manaMaxValue = stats[UIStats.MANA_MAX] ?: "1"
            val manaPercentage = manaValue.toFloat() / manaMaxValue.toFloat()
            manaMenuItemLabel.value("$manaValue/$manaMaxValue")
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
