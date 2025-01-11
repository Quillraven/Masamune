package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import io.github.masamune.ui.model.I18NKey
import io.github.masamune.ui.model.StatsViewModel
import io.github.masamune.ui.model.UIStats
import io.github.masamune.ui.widget.MenuItemLabel
import io.github.masamune.ui.widget.MenuItemStatsLabel
import io.github.masamune.ui.widget.frameImage
import io.github.masamune.ui.widget.menuItemLabel
import io.github.masamune.ui.widget.menuItemStatsLabel
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

    private val strengthMenuItemLabel: MenuItemStatsLabel
    private val agilityMenuItemLabel: MenuItemStatsLabel
    private val constitutionMenuItemLabel: MenuItemStatsLabel
    private val intelligenceMenuItemLabel: MenuItemStatsLabel

    private val attackMenuItemLabel: MenuItemStatsLabel
    private val armorMenuItemLabel: MenuItemStatsLabel
    private val resistanceMenuItemLabel: MenuItemStatsLabel

    private val physicalEvadeMenuItemLabel: MenuItemStatsLabel
    private val magicalEvadeMenuItemLabel: MenuItemStatsLabel
    private val criticalStrikeMenuItemLabel: MenuItemStatsLabel
    private val arcaneStrikeMenuItemLabel: MenuItemStatsLabel

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
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_STRENGTH), "") { cell ->
                    cell.row()
                }
            this@StatsView.agilityMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_AGILITY), "") { cell ->
                    cell.row()
                }
            this@StatsView.constitutionMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_CONSTITUTION), "") { cell ->
                    cell.row()
                }
            this@StatsView.intelligenceMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_INTELLIGENCE), "") { cell ->
                    cell.padBottom(CATEGORY_BOT_PADDING).row()
                }

            // Attack, Armor, Resistance
            this@StatsView.attackMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_ATTACK), "") { cell ->
                    cell.row()
                }
            this@StatsView.armorMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_ARMOR), "") { cell ->
                    cell.row()
                }
            this@StatsView.resistanceMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_RESISTANCE), "") { cell ->
                    cell.padBottom(CATEGORY_BOT_PADDING).row()
                }

            // Percentage stats (phys. + magical evasion, phys. + magical critical strike)
            this@StatsView.physicalEvadeMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_PHYSICAL_EVADE), "") { cell ->
                    cell.row()
                }
            this@StatsView.magicalEvadeMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_MAGICAL_EVADE), "") { cell ->
                    cell.row()
                }
            this@StatsView.criticalStrikeMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_CRITICAL_STRIKE), "") { cell ->
                    cell.row()
                }
            this@StatsView.arcaneStrikeMenuItemLabel =
                menuItemStatsLabel(skin, this@StatsView.i18nTxt(I18NKey.STATS_ARCANE_STRIKE), "") { cell ->
                    cell.row()
                }

            rightTableCell.grow().pad(EDGE_PADDING, 0f, EDGE_PADDING, EDGE_PADDING)
        }

        registerOnPropertyChanges()
    }

    override fun registerOnPropertyChanges() {
        viewModel.onPropertyChange(StatsViewModel::playerName) { name ->
            isVisible = name.isNotBlank()
            nameLabel.txt = name
        }
        viewModel.onPropertyChange(StatsViewModel::playerStats) { stats ->
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
            strengthMenuItemLabel.valueAndDiff(stats[UIStats.STRENGTH] ?: missingValue, viewModel.equipmentBonus(UIStats.STRENGTH))
            agilityMenuItemLabel.valueAndDiff(stats[UIStats.AGILITY] ?: missingValue, viewModel.equipmentBonus(UIStats.AGILITY))
            constitutionMenuItemLabel.valueAndDiff(stats[UIStats.CONSTITUTION] ?: missingValue, viewModel.equipmentBonus(UIStats.CONSTITUTION))
            intelligenceMenuItemLabel.valueAndDiff(stats[UIStats.INTELLIGENCE] ?: missingValue, viewModel.equipmentBonus(UIStats.INTELLIGENCE))

            attackMenuItemLabel.valueAndDiff(stats[UIStats.DAMAGE] ?: missingValue, viewModel.equipmentBonus(UIStats.DAMAGE))
            armorMenuItemLabel.valueAndDiff(stats[UIStats.ARMOR] ?: missingValue, viewModel.equipmentBonus(UIStats.ARMOR))
            resistanceMenuItemLabel.valueAndDiff(stats[UIStats.RESISTANCE] ?: missingValue, viewModel.equipmentBonus(UIStats.RESISTANCE))

            physicalEvadeMenuItemLabel.valueAndDiff(stats[UIStats.PHYSICAL_EVADE] ?: missingValue, viewModel.equipmentBonus(UIStats.PHYSICAL_EVADE))
            magicalEvadeMenuItemLabel.valueAndDiff(stats[UIStats.MAGICAL_EVADE] ?: missingValue, viewModel.equipmentBonus(UIStats.MAGICAL_EVADE))
            criticalStrikeMenuItemLabel.valueAndDiff(stats[UIStats.CRITICAL_STRIKE] ?: missingValue, viewModel.equipmentBonus(UIStats.CRITICAL_STRIKE))
            arcaneStrikeMenuItemLabel.valueAndDiff(stats[UIStats.ARCANE_STRIKE] ?: missingValue, viewModel.equipmentBonus(UIStats.ARCANE_STRIKE))

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
