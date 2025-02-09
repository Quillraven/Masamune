package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.rafaskoberg.gdx.typinglabel.TypingLabel
import io.github.masamune.ui.model.UIStats
import io.github.masamune.ui.view.typingLabel
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.table

@Scene2dDsl
class MonsterInfoTable(
    skin: Skin,
    statsLabels: Map<UIStats, String>,
) : Table(skin), KTable {

    private val image: Image
    private val nameLabel: Label
    private val lifeLabel: Label
    private val manaLabel: Label
    private val agilityLabel: Label
    private val damageLabel: Label
    private val armorLabel: Label
    private val resistanceLabel: Label
    private val xpLabel: Label
    private val talonsLabel: Label
    private val descriptionLabel: TypingLabel

    init {
        background = skin.getDrawable("dialog_frame")

        table(skin) { headerTblCell ->
            this@MonsterInfoTable.image = image { imgCell ->
                setScaling(Scaling.fit)
                imgCell.size(42f, 42f).fill()
            }
            this@MonsterInfoTable.nameLabel = label("", "dialog_content", skin) { lblCell ->
                setAlignment(Align.left)
                color = skin.getColor("highlight_blue")
                lblCell.growX().padLeft(20f)
            }
            headerTblCell.growX().padBottom(20f).row()
        }

        table(skin) { statsTblCell ->
            label("${statsLabels[UIStats.LIFE]}:", "dialog_content", skin) {
                this.color = skin.getColor("green")
                this.setAlignment(Align.left)
                it.fillX().padLeft(10f)
            }
            this@MonsterInfoTable.lifeLabel = label("", "dialog_content", skin) { lblCell ->
                this.color = skin.getColor("dark_grey")
                lblCell.growX().padLeft(15f).row()
            }

            label("${statsLabels[UIStats.MANA]}:", "dialog_content", skin) {
                this.color = skin.getColor("blue")
                this.setAlignment(Align.left)
                it.fillX().padLeft(10f)
            }
            this@MonsterInfoTable.manaLabel = label("", "dialog_content", skin) { lblCell ->
                this.color = skin.getColor("dark_grey")
                lblCell.growX().padLeft(15f).row()
            }

            label("${statsLabels[UIStats.AGILITY]}:", "dialog_content", skin) {
                this.color = skin.getColor("dark_grey")
                this.setAlignment(Align.left)
                it.fillX().padLeft(10f)
            }
            this@MonsterInfoTable.agilityLabel = label("", "dialog_content", skin) { lblCell ->
                this.color = skin.getColor("dark_grey")
                lblCell.growX().padLeft(15f).row()
            }

            label("${statsLabels[UIStats.DAMAGE]}:", "dialog_content", skin) {
                this.color = skin.getColor("dark_grey")
                this.setAlignment(Align.left)
                it.fillX().padLeft(10f)
            }
            this@MonsterInfoTable.damageLabel = label("", "dialog_content", skin) { lblCell ->
                this.color = skin.getColor("dark_grey")
                lblCell.growX().padLeft(15f).row()
            }

            label("${statsLabels[UIStats.ARMOR]}:", "dialog_content", skin) {
                this.color = skin.getColor("dark_grey")
                this.setAlignment(Align.left)
                it.fillX().padLeft(10f)
            }
            this@MonsterInfoTable.armorLabel = label("", "dialog_content", skin) { lblCell ->
                this.color = skin.getColor("dark_grey")
                lblCell.growX().padLeft(15f).row()
            }

            label("${statsLabels[UIStats.RESISTANCE]}:", "dialog_content", skin) {
                this.color = skin.getColor("dark_grey")
                this.setAlignment(Align.left)
                it.fillX().padLeft(10f)
            }
            this@MonsterInfoTable.resistanceLabel = label("", "dialog_content", skin) { lblCell ->
                this.color = skin.getColor("dark_grey")
                lblCell.growX().padLeft(15f).row()
            }

            label("${statsLabels[UIStats.XP]}:", "dialog_content", skin) {
                this.color = skin.getColor("dark_grey")
                this.setAlignment(Align.left)
                it.fillX().padLeft(10f)
            }
            this@MonsterInfoTable.xpLabel = label("", "dialog_content", skin) { lblCell ->
                this.color = skin.getColor("dark_grey")
                lblCell.growX().padLeft(15f).row()
            }

            label("${statsLabels[UIStats.TALONS]}:", "dialog_content", skin) {
                this.color = skin.getColor("dark_grey")
                this.setAlignment(Align.left)
                it.fillX().padLeft(10f)
            }
            this@MonsterInfoTable.talonsLabel = label("", "dialog_content", skin) { lblCell ->
                this.color = skin.getColor("dark_grey")
                lblCell.growX().padLeft(15f).row()
            }

            statsTblCell.growX().padBottom(20f).row()
        }

        descriptionLabel = typingLabel("", "dialog_content", skin) { lblCell ->
            wrap = true
            val defaultTextColor = skin.getColor("dark_grey")
            defaultToken = "{COLOR=#$defaultTextColor}"
            clearColor.set(defaultTextColor)
            setAlignment(Align.topLeft)
            lblCell.padTop(10f).grow()
        }
    }

    fun monster(name: String, description: String, drawable: Drawable, stats: Map<UIStats, String>) {
        this.nameLabel.txt = name
        this.descriptionLabel.restart("{FAST}$description")
        this.image.drawable = drawable
        this.lifeLabel.txt = "${stats[UIStats.LIFE_MAX]}"
        this.manaLabel.txt = "${stats[UIStats.MANA_MAX]}"
        this.agilityLabel.txt = "${stats[UIStats.AGILITY]}"
        this.damageLabel.txt = "${stats[UIStats.DAMAGE]}"
        this.armorLabel.txt = "${stats[UIStats.ARMOR]}"
        this.resistanceLabel.txt = "${stats[UIStats.RESISTANCE]}"
        this.xpLabel.txt = "${stats[UIStats.XP]}"
        this.talonsLabel.txt = "${stats[UIStats.TALONS]}"
    }

    fun clearMonster() {
        this.nameLabel.txt = ""
        this.descriptionLabel.txt = ""
        this.lifeLabel.txt = ""
        this.manaLabel.txt = ""
        this.agilityLabel.txt = ""
        this.damageLabel.txt = ""
        this.armorLabel.txt = ""
        this.resistanceLabel.txt = ""
        this.xpLabel.txt = ""
        this.talonsLabel.txt = ""
        this.image.drawable = null
    }
}

@Scene2dDsl
fun <S> KWidget<S>.monsterInfoTable(
    skin: Skin,
    statsLabels: Map<UIStats, String>,
    init: (@Scene2dDsl MonsterInfoTable).(S) -> Unit = {},
): MonsterInfoTable = actor(MonsterInfoTable(skin, statsLabels), init)
