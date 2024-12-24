package io.github.masamune.ui.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import io.github.masamune.ui.model.CombatViewModel
import io.github.masamune.ui.model.ItemCombatModel
import io.github.masamune.ui.model.MagicModel
import io.github.masamune.ui.widget.ItemCombatTable
import io.github.masamune.ui.widget.MagicTable
import io.github.masamune.ui.widget.itemCombatTable
import io.github.masamune.ui.widget.magicTable
import ktx.actors.plusAssign
import ktx.actors.then
import ktx.actors.txt
import ktx.scene2d.KButtonTable
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.buttonGroup
import ktx.scene2d.defaultStyle
import ktx.scene2d.imageButton
import ktx.scene2d.label
import ktx.scene2d.progressBar
import ktx.scene2d.scene2d
import ktx.scene2d.table
import kotlin.math.max

private enum class UiAction {
    UNDEFINED, ATTACK, MAGIC, ITEM
}

private enum class UiCombatState {
    SELECT_ACTION, SELECT_TARGET, SELECT_MAGIC, SELECT_ITEM
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

    private val actionTable: KButtonTable
    private val attackBtn: ImageButton
    private val magicBtn: ImageButton
    private val itemBtn: ImageButton

    private val magicTable: MagicTable
    private var magicModels: List<MagicModel> = listOf()

    private val itemTable: ItemCombatTable
    private var itemModels: List<ItemCombatModel> = listOf()

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

        actionTable = buttonGroup(minCheckedCount = 0, maxCheckedCount = 1, skin) {
            this@CombatView.attackBtn = imageButton("imgBtnSword", skin) {
                image.setScaling(Scaling.fit)
                imageCell.size(ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD, ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD)
                this.isChecked = false
                it.colspan(2).center().size(ACTION_BTN_SIZE, ACTION_BTN_SIZE).row()
            }
            this@CombatView.magicBtn = imageButton("imgBtnMagic", skin) {
                image.setScaling(Scaling.fit)
                imageCell.size(ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD, ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD)
                this.isChecked = false
                it.padRight(ACTION_BTN_SIZE * 0.5f).size(ACTION_BTN_SIZE, ACTION_BTN_SIZE)
            }
            this@CombatView.itemBtn = imageButton("imgBtnItems", skin) {
                image.setScaling(Scaling.fit)
                imageCell.size(ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD, ACTION_BTN_SIZE - ACTION_BTN_IMG_PAD)
                this.isChecked = false
                it.padLeft(ACTION_BTN_SIZE * 0.5f).size(ACTION_BTN_SIZE, ACTION_BTN_SIZE)
            }

            pack()
        }

        magicTable = magicTable(skin) { this.isVisible = false }

        itemTable = itemCombatTable(skin) { this.isVisible = false }

        registerOnPropertyChanges(model)
    }

    private fun registerOnPropertyChanges(model: CombatViewModel) {
        // player related property events
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
            magicList.forEach { (_, name, targetDescriptor, mana, canPerform) ->
                magicTable.magic(name, targetDescriptor, mana, canPerform)
            }
            magicTable.pack()
            magicTable.height = 300f
            magicTable.width = max(magicTable.width, 400f)
        }
        model.onPropertyChange(CombatViewModel::playerItems) { itemList ->
            itemModels = itemList
            itemTable.clearItems()
            itemList.forEach { (_, name, targetDescriptor, amount) ->
                itemTable.item(name, targetDescriptor, amount)
            }
            itemTable.pack()
            itemTable.height = 300f
            itemTable.width = max(itemTable.width, 400f)
        }

        // ui position update
        model.onPropertyChange(CombatViewModel::playerPosition) { position ->
            val infoW = playerInfoTable.width
            playerInfoTable.setPosition(position.x - infoW, 5f)
            playerInfoTable.toFront()
            val actionW = actionTable.width
            actionTable.setPosition(
                position.x - infoW * 0.5f - actionW * 0.5f,
                playerInfoTable.height - actionTable.height * 0.7f
            )
            magicTable.setPosition(
                position.x - infoW * 0.5f - magicTable.width * 0.5f,
                playerInfoTable.height + 20f + actionTable.height
            )
            itemTable.setPosition(
                position.x - infoW * 0.5f - itemTable.width * 0.5f,
                playerInfoTable.height + 20f + actionTable.height
            )
        }

        // action table fade in effect
        model.onPropertyChange(CombatViewModel::combatTurn) {
            playerInfoTable.height + 10f
            actionTable.actions.clear()
            actionTable += Actions.moveBy(0f, actionTable.height * 0.7f + 10f, 1f, Interpolation.bounceIn)
            actionTable.buttonGroup.uncheckAll()
            uiAction = UiAction.UNDEFINED
        }

        // damage/heal/mana indicators
        model.onPropertyChange(CombatViewModel::combatDamage) { (position, amount) ->
            combatTxt(amount, "{JUMP=1.5;5;1;1}", skin.getColor("red"), position, 2.5f)
        }
        model.onPropertyChange(CombatViewModel::combatHeal) { (position, amount) ->
            combatTxt(amount, "{JUMP=0.5;5;1;1}", skin.getColor("green"), position, 2f)
        }
        model.onPropertyChange(CombatViewModel::combatMana) { (position, amount) ->
            combatTxt(amount, "{JUMP=0.5;5;1;1}", skin.getColor("blue"), position, 2f)
        }
    }

    private fun combatTxt(amount: Int, effect: String, color: Color, position: Vector2, duration: Float) {
        val label = scene2d.typingLabel("$effect$amount", "combat_number", skin) {
            this.color = color
            this.setPosition(position.x, position.y)
        }
        val transparentColor = Color(color.r, color.g, color.b, 0f)
        label += Actions.color(transparentColor, duration, Interpolation.pow3OutInverse) then Actions.removeActor()
        stage.addActor(label)
    }

    override fun onUpPressed() {
        when (uiState) {
            UiCombatState.SELECT_ACTION -> {
                uiAction = UiAction.ATTACK
                attackBtn.isChecked = true
                viewModel.playSndMenuClick()
            }

            UiCombatState.SELECT_MAGIC -> {
                magicTable.prevMagic(MagicTable.MAGIC_PER_ROW)
                viewModel.playSndMenuClick()
            }

            else -> Unit
        }
    }

    override fun onLeftPressed() {
        when (uiState) {
            UiCombatState.SELECT_ACTION -> {
                uiAction = UiAction.MAGIC
                magicBtn.isChecked = true
                viewModel.playSndMenuClick()
            }

            UiCombatState.SELECT_TARGET -> viewModel.selectPrevTarget()
            UiCombatState.SELECT_MAGIC -> {
                magicTable.prevMagic()
                viewModel.playSndMenuClick()
            }
            UiCombatState.SELECT_ITEM -> {
                itemTable.prevItem()
                viewModel.playSndMenuClick()
            }
        }
    }

    override fun onRightPressed() {
        when (uiState) {
            UiCombatState.SELECT_ACTION -> {
                uiAction = UiAction.ITEM
                itemBtn.isChecked = true
                viewModel.playSndMenuClick()
            }

            UiCombatState.SELECT_TARGET -> viewModel.selectNextTarget()
            UiCombatState.SELECT_MAGIC -> {
                magicTable.nextMagic()
                viewModel.playSndMenuClick()
            }
            UiCombatState.SELECT_ITEM -> {
                itemTable.nextItem()
                viewModel.playSndMenuClick()
            }
        }
    }

    override fun onDownPressed() {
        when (uiState) {
            UiCombatState.SELECT_MAGIC -> {
                magicTable.nextMagic(MagicTable.MAGIC_PER_ROW)
                viewModel.playSndMenuClick()
            }

            UiCombatState.SELECT_ITEM -> {
                itemTable.nextItem(ItemCombatTable.ITEMS_PER_ROW)
                viewModel.playSndMenuClick()
            }

            else -> Unit
        }
    }

    override fun onBackPressed() {
        when (uiState) {
            UiCombatState.SELECT_TARGET -> {
                if (viewModel.stopOrRevertSelection()) {
                    uiState = UiCombatState.SELECT_ACTION
                }
            }

            UiCombatState.SELECT_MAGIC -> {
                magicTable.isVisible = false
                uiState = UiCombatState.SELECT_ACTION
                viewModel.playSndMenuAbort()
            }

            UiCombatState.SELECT_ITEM -> {
                itemTable.isVisible = false
                uiState = UiCombatState.SELECT_ACTION
                viewModel.playSndMenuAbort()
            }

            UiCombatState.SELECT_ACTION -> Unit
        }
    }

    override fun onSelectPressed() {
        when (uiState) {
            UiCombatState.SELECT_ACTION -> when (uiAction) {
                UiAction.ATTACK -> {
                    uiState = UiCombatState.SELECT_TARGET
                    viewModel.selectAttack()
                }

                UiAction.MAGIC -> {
                    uiState = UiCombatState.SELECT_MAGIC
                    magicTable.isVisible = true
                    magicTable.selectFirstMagic()
                    viewModel.playSndMenuAccept()
                }

                UiAction.ITEM -> {
                    uiState = UiCombatState.SELECT_ITEM
                    itemTable.isVisible = true
                    itemTable.selectFirstItem()
                    viewModel.playSndMenuAccept()
                }

                else -> Unit
            }

            UiCombatState.SELECT_TARGET -> {
                if (viewModel.confirmTargetSelection()) {
                    uiState = UiCombatState.SELECT_ACTION
                    actionTable.buttonGroup.uncheckAll()
                    // hide action table again
                    actionTable.actions.clear()
                    actionTable += Actions.moveBy(0f, -actionTable.height * 0.7f - 10f, 1f, Interpolation.bounceOut)
                }
            }

            UiCombatState.SELECT_MAGIC -> {
                if (magicTable.hasNoMagic()) {
                    // cannot perform any magic -> do nothing
                    return
                }

                uiState = UiCombatState.SELECT_TARGET
                viewModel.selectMagic(magicModels[magicTable.selectedMagic])
                magicTable.isVisible = false
            }

            UiCombatState.SELECT_ITEM -> {
                if (itemTable.hasNoItem()) {
                    // no items to use -> do nothing
                    return
                }

                uiState = UiCombatState.SELECT_TARGET
                viewModel.selectItem(itemModels[itemTable.selectedItem])
                itemTable.isVisible = false
            }
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
