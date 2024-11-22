package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.masamune.ui.model.UIStats
import ktx.actors.txt
import ktx.app.gdxError
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import ktx.scene2d.label
import ktx.scene2d.scene2d

class ShopStatsTable(
    skin: Skin,
    shopTitle: String,
    statsLabels: Map<UIStats, String>,
) : KTable, Table(skin) {

    private val playerStats: Map<UIStats, ShopStatsLabel>
    private val shopNameLabel: Label

    init {
        // header: Shop Name + character faces
        shopNameLabel = label(shopTitle, "dialog_image_caption", skin) {
            setAlignment(Align.center)
        }
        frameImage(skin, "dialog_face_frame", "hero") { cell ->
            cell.row()
        }

        playerStats = mapOf(
            UIStats.STRENGTH to statsRow(skin, statsLabels[UIStats.STRENGTH] ?: ""),
            UIStats.AGILITY to statsRow(skin, statsLabels[UIStats.AGILITY] ?: ""),
            UIStats.CONSTITUTION to statsRow(skin, statsLabels[UIStats.CONSTITUTION] ?: ""),
            UIStats.INTELLIGENCE to statsRow(skin, statsLabels[UIStats.INTELLIGENCE] ?: ""),
            UIStats.ATTACK to statsRow(skin, statsLabels[UIStats.ATTACK] ?: ""),
            UIStats.ARMOR to statsRow(skin, statsLabels[UIStats.ARMOR] ?: ""),
            UIStats.RESISTANCE to statsRow(skin, statsLabels[UIStats.RESISTANCE] ?: ""),
        )
    }

    private fun KTable.statsRow(skin: Skin, statLabel: String): ShopStatsLabel {
        val label = scene2d.label(statLabel, defaultStyle, skin) {
            color = skin.getColor("dark_grey")
            setAlignment(Align.left)
        }
        val statsLabel = scene2d.shopStatsLabel(skin, "")
        add(label).left()
        add(statsLabel).left().padLeft(50f).fillX().row()
        return statsLabel
    }

    fun statsValue(stat: UIStats, value: String, diff: Int = 0) {
        val statsLabel = playerStats[stat] ?: gdxError("No StatsLabel for $stat")
        statsLabel.txt(value, diff)
    }

    fun shopName(name: String) {
        shopNameLabel.txt = name
    }
}

@Scene2dDsl
fun <S> KWidget<S>.shopStatsTable(
    skin: Skin,
    shopTitle: String,
    statsLabels: Map<UIStats, String>,
    init: (@Scene2dDsl ShopStatsTable).(S) -> Unit = {},
): ShopStatsTable = actor(ShopStatsTable(skin, shopTitle, statsLabels), init)
