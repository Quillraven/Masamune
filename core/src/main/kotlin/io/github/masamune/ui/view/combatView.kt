package io.github.masamune.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import io.github.masamune.ui.model.CombatViewModel
import io.github.masamune.ui.model.MagicModel
import io.github.masamune.ui.widget.MagicTable
import io.github.masamune.ui.widget.magicTable
import ktx.actors.txt
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.defaultStyle
import ktx.scene2d.imageButton
import ktx.scene2d.label
import ktx.scene2d.progressBar
import ktx.scene2d.table

private enum class UiAction {
    UNDEFINED, ATTACK, MAGIC, ITEM
}

private enum class UiCombatState {
    SELECT_ACTION, SELECT_TARGET, SELECT_MAGIC
}

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

    private val actionTable: Table
    private val attackBtn: ImageButton
    private val magicBtn: ImageButton
    private val itemBtn: ImageButton

    private val magicTable: MagicTable
    private var magicModels: List<MagicModel> = listOf()

    private var uiAction: UiAction = UiAction.UNDEFINED
    private var uiState = UiCombatState.SELECT_ACTION

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

        actionTable = table(skin) {
            this@CombatView.attackBtn = imageButton("imgBtnSword", skin) {
                image.setScaling(Scaling.fit)
                imageCell.size(ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD, ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD)
                this.isChecked = true
                it.colspan(2).center().size(ACTION_BTN_SIZE, ACTION_BTN_SIZE).row()
            }
            this@CombatView.magicBtn = imageButton("imgBtnMagic", skin) {
                image.setScaling(Scaling.fit)
                imageCell.size(ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD, ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD)
                this.isChecked = true
                it.padRight(ACTION_BTN_SIZE * 0.5f).size(ACTION_BTN_SIZE, ACTION_BTN_SIZE)
            }
            this@CombatView.itemBtn = imageButton("imgBtnItems", skin) {
                image.setScaling(Scaling.fit)
                imageCell.size(ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD, ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD)
                this.isChecked = true
                it.padLeft(ACTION_BTN_SIZE * 0.5f).size(ACTION_BTN_SIZE, ACTION_BTN_SIZE)
            }

            pack()
        }

        magicTable = magicTable(skin) { this.isVisible = false }

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
        model.onPropertyChange(CombatViewModel::playerMagic) { magicList ->
            magicModels = magicList
            magicTable.clearMagic()
            magicList.forEach { magic -> magicTable.magic(magic.name, magic.targetDescriptor, magic.mana) }
            magicTable.pack()
            magicTable.height = 300f
        }
        model.onPropertyChange(CombatViewModel::playerPosition) { position ->
            val infoW = playerInfoTable.width
            playerInfoTable.setPosition(position.x - infoW, 5f)
            val actionW = actionTable.width
            actionTable.setPosition(position.x - infoW * 0.5f - actionW * 0.5f, playerInfoTable.height + 10f)
            magicTable.setPosition(
                position.x - infoW * 0.5f - magicTable.width * 0.5f,
                playerInfoTable.height + 20f + actionTable.height
            )
        }
    }

    override fun onUpPressed() {
        if (uiState == UiCombatState.SELECT_ACTION) {
            uiAction = UiAction.ATTACK
            attackBtn.isChecked = false
            magicBtn.isChecked = true
            itemBtn.isChecked = true
            viewModel.playSndMenuClick()
        }
    }

    override fun onLeftPressed() {
        if (uiState == UiCombatState.SELECT_TARGET) {
            viewModel.selectPrevTarget()
            return
        } else if (uiState == UiCombatState.SELECT_MAGIC) {
            magicTable.prevMagic()
            viewModel.playSndMenuClick()
            return
        }

        uiAction = UiAction.MAGIC
        attackBtn.isChecked = true
        magicBtn.isChecked = false
        itemBtn.isChecked = true
        viewModel.playSndMenuClick()
    }

    override fun onRightPressed() {
        if (uiState == UiCombatState.SELECT_TARGET) {
            viewModel.selectNextTarget()
            return
        } else if (uiState == UiCombatState.SELECT_MAGIC) {
            magicTable.nextMagic()
            viewModel.playSndMenuClick()
            return
        }

        uiAction = UiAction.ITEM
        attackBtn.isChecked = true
        magicBtn.isChecked = true
        itemBtn.isChecked = false
        viewModel.playSndMenuClick()
    }

    override fun onBackPressed() {
        if (uiState == UiCombatState.SELECT_TARGET) {
            if (viewModel.stopOrRevertSelection()) {
                uiState = UiCombatState.SELECT_ACTION
            }
            return
        } else if (uiState == UiCombatState.SELECT_MAGIC) {
            magicTable.isVisible = false
            uiState = UiCombatState.SELECT_ACTION
            viewModel.playSndMenuAbort()
        }
    }

    override fun onSelectPressed() {
        if (uiState == UiCombatState.SELECT_TARGET) {
            if (viewModel.confirmTargetSelection()) {
                uiState = UiCombatState.SELECT_ACTION
                attackBtn.isChecked = true
                magicBtn.isChecked = true
                itemBtn.isChecked = true
            }
            return
        } else if (uiState == UiCombatState.SELECT_MAGIC) {
            uiState = UiCombatState.SELECT_TARGET
            viewModel.selectMagic(magicModels[magicTable.selectedMagic])
            magicTable.isVisible = false
            return
        }

        viewModel.playSndMenuAccept()
        when (uiAction) {
            UiAction.ATTACK -> {
                uiState = UiCombatState.SELECT_TARGET
                viewModel.selectAttack()
            }

            UiAction.MAGIC -> {
                uiState = UiCombatState.SELECT_MAGIC
                magicTable.isVisible = true
            }

            else -> Unit
        }
    }

    companion object {
        private const val ACTION_BTN_SIZE = 30f
        private const val ACTION_BTN_IMG_PAD = 10f
    }
}

@Scene2dDsl
fun <S> KWidget<S>.combatView(
    model: CombatViewModel,
    skin: Skin,
    init: (@Scene2dDsl CombatView).(S) -> Unit = {},
): CombatView = actor(CombatView(model, skin), init)
