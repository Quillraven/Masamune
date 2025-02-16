package io.github.masamune.ui.view

import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.actions.Actions.forever
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Scaling
import io.github.masamune.ui.model.MainMenuViewModel
import ktx.actors.plusAssign
import ktx.actors.then
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.progressBar
import ktx.scene2d.table


@Scene2dDsl
class MainMenuView(
    model: MainMenuViewModel,
    skin: Skin,
) : View<MainMenuViewModel>(skin, model), KTable {

    private val startGameLabel: Label
    private val musicBar: ProgressBar
    private val soundBar: ProgressBar
    private val quitGameLabel: Label
    private var currentSelection: Table

    init {
        left().bottom().padBottom(30f).padLeft(10f)
        setFillParent(true)

        table(skin) {
            this.background = skin.getDrawable("nine_path_bg_2")
            this.defaults().pad(10f)

            this@MainMenuView.currentSelection = table(skin) { startGameTableCell ->
                image(skin.getDrawable("arrow")) { cell ->
                    setScaling(Scaling.contain)
                    this += forever(fadeOut(0.5f) then fadeIn(0.25f) then delay(0.25f))
                    cell.padRight(5f).left()
                    this.name = "selection"
                }
                this@MainMenuView.startGameLabel = label("Start Game", "dialog_option", skin) {
                    it.expandX()
                }
                startGameTableCell.uniformX().growX().row()
            }

            table(skin) { pbTableCell ->
                this.left()
                label("Music Volume", "dialog_content", skin) {
                    it.padBottom(5f).colspan(2).row()
                }
                image(skin.getDrawable("arrow")) { cell ->
                    setScaling(Scaling.contain)
                    this += forever(fadeOut(0.5f) then fadeIn(0.25f) then delay(0.25f))
                    cell.padRight(5f).left()
                    this.isVisible = false
                    this.name = "selection"
                }
                this@MainMenuView.musicBar = progressBar(0f, 1f, 0.05f, false, "yellow", skin) {
                    this.value = model.musicVolume
                    this.name = "volumePB"
                    it.width(200f)
                }
                pbTableCell.uniformX().growX().row()
            }

            table(skin) { pbTableCell ->
                this.left()
                label("Sound Volume", "dialog_content", skin) {
                    it.padBottom(5f).colspan(2).row()
                }
                image(skin.getDrawable("arrow")) { cell ->
                    setScaling(Scaling.contain)
                    this += forever(fadeOut(0.5f) then fadeIn(0.25f) then delay(0.25f))
                    cell.padRight(5f).left()
                    this.isVisible = false
                    this.name = "selection"
                }
                this@MainMenuView.soundBar = progressBar(0f, 1f, 0.05f, false, "yellow", skin) {
                    this.value = model.soundVolume
                    this.name = "volumePB"
                    it.width(200f)
                }
                pbTableCell.uniformX().growX().row()
            }

            table(skin) { quitGameTableCell ->
                image(skin.getDrawable("arrow")) { cell ->
                    setScaling(Scaling.contain)
                    this += forever(fadeOut(0.5f) then fadeIn(0.25f) then delay(0.25f))
                    cell.padRight(5f).left()
                    this.isVisible = false
                    this.name = "selection"
                }
                this@MainMenuView.quitGameLabel = label("Quit Game", "dialog_option", skin) {
                    it.expandX()
                }
                quitGameTableCell.uniformX().growX()
            }
        }

        registerOnPropertyChanges()
    }

    override fun registerOnPropertyChanges() = Unit

    override fun onUpPressed() {
        val parentTable = currentSelection.parent
        var currentIndex = parentTable.children.indexOf(currentSelection)
        if (currentIndex <= 0) {
            return
        }

        currentSelection.findActor<Image>("selection").isVisible = false
        currentIndex -= 1
        currentSelection = parentTable.getChild(currentIndex) as Table
        currentSelection.findActor<Image>("selection").isVisible = true
        viewModel.playSndMenuClick()
    }

    override fun onDownPressed() {
        val parentTable = currentSelection.parent
        var currentIndex = parentTable.children.indexOf(currentSelection)
        if (currentIndex >= parentTable.children.size - 1) {
            return
        }

        currentSelection.findActor<Image>("selection").isVisible = false
        currentIndex += 1
        currentSelection = parentTable.getChild(currentIndex) as Table
        currentSelection.findActor<Image>("selection").isVisible = true
        viewModel.playSndMenuClick()
    }

    override fun onRightPressed() {
        val parentTable = currentSelection.parent
        when (parentTable.children.indexOf(currentSelection)) {
            1 -> {
                val pb = currentSelection.findActor<ProgressBar>("volumePB")
                pb.setValue(pb.value + pb.stepSize)
                viewModel.musicVolume = pb.value
            }

            2 -> {
                val pb = currentSelection.findActor<ProgressBar>("volumePB")
                pb.setValue(pb.value + pb.stepSize)
                viewModel.soundVolume = pb.value
                viewModel.playSndMenuClick()
            }
        }
    }

    override fun onLeftPressed() {

        val parentTable = currentSelection.parent
        when (parentTable.children.indexOf(currentSelection)) {
            1 -> {
                val pb = currentSelection.findActor<ProgressBar>("volumePB")
                pb.setValue(pb.value - pb.stepSize)
                viewModel.musicVolume = pb.value
            }

            2 -> {
                val pb = currentSelection.findActor<ProgressBar>("volumePB")
                pb.setValue(pb.value - pb.stepSize)
                viewModel.soundVolume = pb.value
                viewModel.playSndMenuClick()
            }
        }
    }

    override fun onSelectPressed() {
        val parentTable = currentSelection.parent
        when (parentTable.children.indexOf(currentSelection)) {
            0 -> viewModel.startGame()
            3 -> viewModel.quitGame()
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.mainMenuView(
    model: MainMenuViewModel,
    skin: Skin,
    init: (@Scene2dDsl MainMenuView).(S) -> Unit = {},
): MainMenuView = actor(MainMenuView(model, skin), init)
