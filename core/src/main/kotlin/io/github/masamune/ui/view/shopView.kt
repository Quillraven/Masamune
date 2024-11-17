package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.masamune.ui.model.ShopViewModel
import io.github.masamune.ui.model.UIStats
import io.github.masamune.ui.widget.ShopStatsLabel
import io.github.masamune.ui.widget.frameImage
import io.github.masamune.ui.widget.optionTable
import io.github.masamune.ui.widget.shopStatsLabel
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.scrollPane
import ktx.scene2d.table

@Scene2dDsl
class ShopView(
    model: ShopViewModel,
    skin: Skin,
) : View<ShopViewModel>(skin, model), KTable {

    private val talonLabel: Label

    private val strLabel: Label
    private val strShopStatsLabel: ShopStatsLabel
    private val agiLabel: Label
    private val agiShopStatsLabel: ShopStatsLabel
    private val intLabel: Label
    private val intShopStatsLabel: ShopStatsLabel
    private val constLabel: Label
    private val constShopStatsLabel: ShopStatsLabel
    private val attackLabel: Label
    private val attackShopStatsLabel: ShopStatsLabel
    private val armorLabel: Label
    private val armorShopStatsLabel: ShopStatsLabel
    private val resistanceLabel: Label
    private val resistanceShopStatsLabel: ShopStatsLabel

    init {
        background = skin.getDrawable("dialog_frame")
        setFillParent(true)

        initTopLeft(skin)
        initTopRight(skin)
        initBottomLeft(skin)
        initBottomRight(skin)

        talonLabel = findActor(ShopView::talonLabel.name)
        strLabel = findActor(ShopView::strLabel.name)
        strShopStatsLabel = findActor(ShopView::strShopStatsLabel.name)
        agiLabel = findActor(ShopView::agiLabel.name)
        agiShopStatsLabel = findActor(ShopView::agiShopStatsLabel.name)
        intLabel = findActor(ShopView::intLabel.name)
        intShopStatsLabel = findActor(ShopView::intShopStatsLabel.name)
        constLabel = findActor(ShopView::constLabel.name)
        constShopStatsLabel = findActor(ShopView::constShopStatsLabel.name)
        attackLabel = findActor(ShopView::attackLabel.name)
        attackShopStatsLabel = findActor(ShopView::attackShopStatsLabel.name)
        armorLabel = findActor(ShopView::armorLabel.name)
        armorShopStatsLabel = findActor(ShopView::armorShopStatsLabel.name)
        resistanceLabel = findActor(ShopView::resistanceLabel.name)
        resistanceShopStatsLabel = findActor(ShopView::resistanceShopStatsLabel.name)

        registerOnPropertyChanges(model)
    }

    private fun statsRow(table: Table, skin: Skin, labelName: String, statsName: String) {
        val label = scene2d.label("", defaultStyle, skin) {
            color = skin.getColor("dark_grey")
            setAlignment(Align.left)
            name = labelName
        }
        val statsLabel = scene2d.shopStatsLabel(skin, "") {
            name = statsName
        }
        table.add(label).left()
        table.add(statsLabel).left().padLeft(50f).fillX().row()
    }

    private fun initTopLeft(skin: Skin) {
        table(skin) { tlTableCell ->

            // header: Shop Name + character faces
            label("Merchant", "dialog_image_caption", skin) {
                setAlignment(Align.center)
            }
            frameImage(skin, "dialog_face_frame", "hero") { cell ->
                cell.row()
            }

            // character stats
            this@ShopView.statsRow(this, skin, ShopView::strLabel.name, ShopView::strShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::agiLabel.name, ShopView::agiShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::constLabel.name, ShopView::constShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::intLabel.name, ShopView::intShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::attackLabel.name, ShopView::attackShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::armorLabel.name, ShopView::armorShopStatsLabel.name)
            this@ShopView.statsRow(this, skin, ShopView::resistanceLabel.name, ShopView::resistanceShopStatsLabel.name)

            tlTableCell.padLeft(10.0f).padTop(10f).top().left()
        }
    }

    private fun initTopRight(skin: Skin) {
        table(skin) { tblCell ->
            background = skin.getDrawable("dialog_frame")

            optionTable(skin) { optionTableCell ->
                optionTableCell.fill().align(Align.left)

                option("Gegenstände")
                option("Verkaufen")
                option("Beenden")
            }

            tblCell.top().right().padTop(10f).padRight(10f).width(250f).row()
        }
    }

    private fun initBottomLeft(skin: Skin) {
        table(skin) { tblCell ->
            background = skin.getDrawable("dialog_frame")

            table(skin) { innerTblCell ->
                label("", "dialog_option", skin) { lblCell ->
                    setAlignment(Align.center)
                    name = ShopView::talonLabel.name
                    lblCell.padTop(2f).padBottom(5f)
                }
                innerTblCell.expandX().top().right().row()
            }

            scrollPane(defaultStyle, skin) { spCell ->
                fadeScrollBars = false
                setFlickScroll(false)
                setForceScroll(false, true)
                setOverscroll(false, false)
                setScrollingDisabled(true, false)

                table(skin) {
                    label("Kleiner Heiltrank", defaultStyle, skin) { lblCell ->
                        lblCell.left().padLeft(5f)
                    }
                    label("100.000K", defaultStyle, skin) { lblCell ->
                        lblCell.padRight(30f).expandX().right()
                    }
                    label("0x", defaultStyle, skin) { lblCell ->
                        lblCell.right().padRight(5f).row()
                    }

                    label("Großer Heiltrank", defaultStyle, skin) { lblCell ->
                        lblCell.left().padLeft(5f)
                    }
                    label("50.000K", defaultStyle, skin) { lblCell ->
                        lblCell.padRight(30f).expandX().right()
                    }
                    label("9x", defaultStyle, skin) { lblCell ->
                        lblCell.right().padRight(5f).row()
                    }

                    label("Kurzschwert", defaultStyle, skin) { lblCell ->
                        lblCell.left().padLeft(5f)
                    }
                    label("100", defaultStyle, skin) { lblCell ->
                        lblCell.padRight(30f).expandX().right()
                    }
                    label("0x", defaultStyle, skin) { lblCell ->
                        lblCell.right().padRight(5f).row()
                    }

                    label("Langschwert", defaultStyle, skin) { lblCell ->
                        lblCell.left().padLeft(5f)
                    }
                    label("550", defaultStyle, skin) { lblCell ->
                        lblCell.padRight(30f).expandX().right()
                    }
                    label("4x", defaultStyle, skin) { lblCell ->
                        lblCell.right().padRight(5f).row()
                    }

                    for (i in 0..20) {
                        label("Item $i", defaultStyle, skin) { lblCell ->
                            lblCell.left().padLeft(5f)
                        }
                        label("${i * 50}", defaultStyle, skin) { lblCell ->
                            lblCell.padRight(30f).expandX().right()
                        }
                        label("0x", defaultStyle, skin) { lblCell ->
                            lblCell.right().padRight(5f).row()
                        }
                    }
                }.top().padTop(5f)

                spCell.grow()
            }

            tblCell.pad(10f, 10f, 10f, 20f).grow()
        }
    }

    private fun initBottomRight(skin: Skin) {
        table(skin) { tblCell ->
            background = skin.getDrawable("dialog_frame")

            table(skin) { headerTblCell ->
                image(skin.getDrawable("flower_girl"))
                label("Kleiner Heiltrank", "dialog_content", skin) { lblCell ->
                    setAlignment(Align.center)
                    color = skin.getColor("highlight_blue")
                    lblCell.growX()
                }
                headerTblCell.growX().row()
            }

            label("Ein kleiner Trank der 25 Leben wiederherstellt.", "dialog_content", skin) { lblCell ->
                wrap = true
                color = skin.getColor("dark_grey")
                setAlignment(Align.topLeft)
                lblCell.padTop(10f).grow()
            }

            tblCell.pad(10f, 0f, 10f, 10f).growX().top()
        }
    }

    private fun registerOnPropertyChanges(model: ShopViewModel) {
        model.onPropertyChange(ShopViewModel::playerStats) { labelsAndStats ->
            val errorTitleLabel = "???" to "0"

            // money
            talonLabel.setText(labelsAndStats[UIStats.TALONS]?.first ?: errorTitleLabel.first)
            // stats
            strLabel.setText(labelsAndStats[UIStats.STRENGTH]?.first ?: errorTitleLabel.first)
            strShopStatsLabel.txt(labelsAndStats[UIStats.STRENGTH]?.second ?: errorTitleLabel.second, 1)
            agiLabel.setText(labelsAndStats[UIStats.AGILITY]?.first ?: errorTitleLabel.first)
            agiShopStatsLabel.txt(labelsAndStats[UIStats.AGILITY]?.second ?: errorTitleLabel.second, 2)
            intLabel.setText(labelsAndStats[UIStats.INTELLIGENCE]?.first ?: errorTitleLabel.first)
            intShopStatsLabel.txt(labelsAndStats[UIStats.INTELLIGENCE]?.second ?: errorTitleLabel.second, 300)
            constLabel.setText(labelsAndStats[UIStats.CONSTITUTION]?.first ?: errorTitleLabel.first)
            constShopStatsLabel.txt(labelsAndStats[UIStats.CONSTITUTION]?.second ?: errorTitleLabel.second, 4)

            attackLabel.setText(labelsAndStats[UIStats.ATTACK]?.first ?: errorTitleLabel.first)
            attackShopStatsLabel.txt(labelsAndStats[UIStats.ATTACK]?.second ?: errorTitleLabel.second, 0)
            armorLabel.setText(labelsAndStats[UIStats.ARMOR]?.first ?: errorTitleLabel.first)
            armorShopStatsLabel.txt(labelsAndStats[UIStats.ARMOR]?.second ?: errorTitleLabel.second, -200)
            resistanceLabel.setText(labelsAndStats[UIStats.RESISTANCE]?.first ?: errorTitleLabel.first)
            resistanceShopStatsLabel.txt(labelsAndStats[UIStats.RESISTANCE]?.second ?: errorTitleLabel.second, -3)
        }
    }

}

@Scene2dDsl
fun <S> KWidget<S>.shopView(
    model: ShopViewModel,
    skin: Skin,
    init: (@Scene2dDsl ShopView).(S) -> Unit = {},
): ShopView = actor(ShopView(model, skin), init)
