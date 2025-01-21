package io.github.masamune.ui.model

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.EntityBagIterator
import com.github.quillraven.fleks.collection.MutableEntityBag
import com.github.quillraven.fleks.collection.compareEntity
import com.github.quillraven.fleks.collection.iterator
import io.github.masamune.SELECTOR_SCALE
import io.github.masamune.SELECTOR_SPEED
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.ActionExecutorService
import io.github.masamune.combat.ActionState
import io.github.masamune.combat.action.Action
import io.github.masamune.combat.action.ActionTargetType
import io.github.masamune.combat.action.UseItemAction
import io.github.masamune.component.Animation
import io.github.masamune.component.Combat
import io.github.masamune.component.Graphic
import io.github.masamune.component.Inventory
import io.github.masamune.component.Item
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Selector
import io.github.masamune.component.CharacterStats
import io.github.masamune.component.Transform
import io.github.masamune.event.CombatEntityHealEvent
import io.github.masamune.event.CombatEntityManaUpdateEvent
import io.github.masamune.event.CombatEntityTakeDamageEvent
import io.github.masamune.event.CombatMissEvent
import io.github.masamune.event.CombatNextTurnEvent
import io.github.masamune.event.CombatPlayerActionEvent
import io.github.masamune.event.CombatPlayerDefeatEvent
import io.github.masamune.event.CombatPlayerVictoryEvent
import io.github.masamune.event.CombatStartEvent
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.event.GameResizeEvent
import io.github.masamune.selectorEntity
import io.github.masamune.tiledmap.ActionType
import io.github.masamune.tiledmap.ItemCategory
import ktx.log.logger
import ktx.math.vec2

class CombatViewModel(
    bundle: I18NBundle,
    audioService: AudioService,
    private val world: World,
    private val eventService: EventService,
    private val gameViewport: Viewport,
    private val uiViewport: Viewport,
    private val actionExecutorService: ActionExecutorService,
) : ViewModel(bundle, audioService) {

    // Separate enemy bag because we sort them by x coordinate which makes it more intuitive for target selection.
    // The left most enemy is the first target while the right most entity is the last target.
    // The normal enemy sorting is by agility which does not make sense for target selection.
    private val enemyEntities = MutableEntityBag(4)
    private val playerEntities = MutableEntityBag(4)
    private val targetComparator = compareEntity(world) { e1, e2 ->
        e1[Transform].position.x.compareTo(e2[Transform].position.x)
    }

    // target selection stuff
    private var targetType = ActionTargetType.NONE
    private val selectorEntities = world.family { all(Selector) }

    // init with any iterator in order for reset to not crash. Iterator will be overridden correctly later on.
    private var selectEntityIterator: EntityBagIterator = selectorEntities.iterator()
    private var activeSelector = Entity.NONE

    // view attributes
    var playerLife: Pair<Float, Float> by propertyNotify(0f to 0f)
    var playerMana: Pair<Float, Float> by propertyNotify(0f to 0f)
    var playerName: String by propertyNotify("")
    var playerPosition: Vector2 by propertyNotify(vec2())
    var playerMagic: List<MagicModel> by propertyNotify(emptyList())
    var playerItems: List<ItemCombatModel> by propertyNotify(emptyList())
    var combatTurn: Int by propertyNotify(-1)
    var combatDamage: Triple<Vector2, Int, Boolean> by propertyNotify(Triple(Vector2.Zero, 0, false))
    var combatHeal: Pair<Vector2, Int> by propertyNotify(Vector2.Zero to 0)
    var combatMana: Pair<Vector2, Int> by propertyNotify(Vector2.Zero to 0)
    var combatMiss: Vector2 by propertyNotify(Vector2.Zero)
    var combatDone: Boolean by propertyNotify(false)

    // special flag to set life/mana bar values instantly in the view instead of using an animation
    var combatStart = false
        private set

    override fun onEvent(event: Event): Unit = with(world) {
        when (event) {
            is CombatStartEvent -> {
                val player = event.player
                // get player stats (life, mana, ...)
                combatStart = true
                val playerStats = player[CharacterStats]
                playerLife = playerStats.life to playerStats.lifeMax
                playerMana = playerStats.mana to playerStats.manaMax
                playerName = player[Name].name
                combatStart = false

                // get player magic
                updatePlayerMagic(player)
                // get player items
                updatePlayerItems(player)

                // store entities for easier target selection
                enemyEntities.clear()
                enemyEntities += event.enemies
                enemyEntities.sort(targetComparator)
                playerEntities.clear()
                playerEntities += player
                playerEntities.sort(targetComparator)

                // position UI elements according to player position
                onResize()
                combatTurn = 0
            }

            is CombatNextTurnEvent -> {
                // update player magic because some of them might not be available due to missing mana or silence
                updatePlayerMagic(event.player)
                // update player items because some of them might have been used in the previous turn
                updatePlayerItems(event.player)

                // update entities because some of them could be dead
                enemyEntities.clear()
                enemyEntities += event.enemies
                enemyEntities.sort(targetComparator)
                playerEntities.clear()
                playerEntities += event.player
                playerEntities.sort(targetComparator)
                ++combatTurn
            }

            is CombatEntityTakeDamageEvent -> {
                if (event.entity has Player) {
                    playerLife = event.life to event.maxLife
                }

                val (position, size) = event.entity[Transform]
                val uiPos = vec2(position.x + MathUtils.random(size.x * 0.1f, size.x * 0.3f), position.y)
                    .toUiPosition(gameViewport, uiViewport)
                combatDamage = Triple(uiPos, event.amount.toInt(), event.critical)
            }

            is CombatEntityHealEvent -> {
                if (event.entity has Player) {
                    playerLife = event.life to event.maxLife
                }

                val (position, size) = event.entity[Transform]
                val uiPos = vec2(position.x + MathUtils.random(size.x * 0.1f, size.x * 0.3f), position.y)
                    .toUiPosition(gameViewport, uiViewport)
                combatHeal = uiPos to event.amount.toInt()
            }

            is CombatMissEvent -> {
                val (position, size) = event.entity[Transform]
                combatMiss = vec2(position.x + MathUtils.random(size.x * 0.1f, size.x * 0.3f), position.y)
                    .toUiPosition(gameViewport, uiViewport)
            }

            is CombatEntityManaUpdateEvent -> {
                if (event.entity has Player) {
                    playerMana = event.mana to event.maxMana
                }

                if (event.state == ActionState.START) {
                    // don't show mana popup for mana cost of action
                    return@with
                }
                val (position, size) = event.entity[Transform]
                val uiPos = vec2(position.x + MathUtils.random(size.x * 0.1f, size.x * 0.3f), position.y)
                    .toUiPosition(gameViewport, uiViewport)
                combatMana = uiPos to event.amount.toInt()
            }

            is CombatPlayerVictoryEvent, is CombatPlayerDefeatEvent -> combatDone = true

            is GameResizeEvent -> onResize()

            else -> Unit
        }
    }

    private fun updatePlayerMagic(player: Entity) = with(world) {
        playerMagic = player[Combat].magicActions.map {
            MagicModel(
                it.type,
                bundle["magic.${it.type.name.lowercase()}.name"],
                bundle["magic.target.${it.targetType.name.lowercase()}"],
                it.manaCost,
                it.run { actionExecutorService.canPerform(player) },
            )
        }
    }

    private fun updatePlayerItems(player: Entity) = with(world) {
        playerItems = player[Inventory].items
            .filter {
                val itemCmp = it[Item]
                itemCmp.category == ItemCategory.OTHER && itemCmp.actionType != ActionType.UNDEFINED && itemCmp.amount > 0
            }
            .map {
                val itemCmp = it[Item]
                ItemCombatModel(
                    itemCmp.type,
                    bundle["item.${itemCmp.type.name.lowercase()}.name"],
                    bundle["magic.target.${itemCmp.action.targetType.name.lowercase()}"],
                    itemCmp.amount,
                )
            }
    }

    private fun onResize() = with(world) {
        // update player position to also trigger an update of view element positions in CombatView
        playerEntities.singleOrNull()?.let { player ->
            val playerPos = player[Transform].position
            playerPosition.set(playerPos.x, playerPos.y).toUiPosition(gameViewport, uiViewport)
            notify(CombatViewModel::playerPosition, playerPosition)
        }
    }

    /**
     * Spawns target selection entities based on the [targetType].
     * If [forEnemy] is true then only enemy entities can be targeted.
     * If [forEnemy] is false then only friendly entities can be targeted.
     */
    private fun spawnTargetSelectorEntities(forEnemy: Boolean) = with(world) {
        selectEntityIterator = if (forEnemy) {
            enemyEntities.iterator()
        } else {
            playerEntities.iterator()
        }

        when (targetType) {
            ActionTargetType.SINGLE -> {
                activeSelector = selectorEntity(selectEntityIterator.next(), true)
            }

            ActionTargetType.MULTI -> {
                activeSelector = selectorEntity(selectEntityIterator.next(), false)
            }

            ActionTargetType.ALL -> {
                while (selectEntityIterator.hasNext()) {
                    selectorEntity(selectEntityIterator.next(), true)
                }
            }

            ActionTargetType.NONE -> Unit
        }
    }

    private fun selectPlayerAction(playerCombat: Combat, action: Action) {
        playerCombat.action = action
        targetType = action.targetType
        spawnTargetSelectorEntities(!action.defensive)
        playSndMenuAccept()
    }

    fun selectAttack() = with(world) {
        val combatCmp = playerEntities.single()[Combat]
        selectPlayerAction(combatCmp, combatCmp.attackAction)
    }

    fun selectMagic(magicModel: MagicModel) = with(world) {
        val combatCmp = playerEntities.single()[Combat]
        selectPlayerAction(combatCmp, combatCmp.magicActions.single { it.type == magicModel.type })
    }

    fun selectItem(itemModel: ItemCombatModel) = with(world) {
        val player = playerEntities.single()
        val inventoryCmp = player[Inventory]
        val item = inventoryCmp.items.single { it[Item].type == itemModel.type }
        val itemCmp = item[Item]
        selectPlayerAction(player[Combat], UseItemAction(item, itemCmp.action.targetType, itemCmp.action.defensive))
    }

    fun selectPrevTarget() {
        if (targetType == ActionTargetType.ALL) {
            return
        }

        playSndMenuClick()
        with(world) {
            activeSelector[Selector].target = selectEntityIterator.previous(loop = true)
            log.debug { "Update selection to: ${selectorEntities.map { it[Selector].target }}" }
        }
    }

    fun selectNextTarget() {
        if (targetType == ActionTargetType.ALL) {
            return
        }

        playSndMenuClick()
        with(world) {
            activeSelector[Selector].target = selectEntityIterator.next(loop = true)
            log.debug { "Update selection to: ${selectorEntities.map { it[Selector].target }}" }
        }
    }

    private fun World.confirmSelector(selector: Entity, confirm: Boolean) {
        selector[Selector].confirmed = confirm
        if (confirm) {
            activeSelector[Transform].scale = 0.8f
            activeSelector[Animation].run {
                speed = 0f
                stateTime = 0f
            }
            activeSelector[Graphic].color.set(1f, 0.5f, 0.5f, 1f)
        } else {
            selector[Animation].speed = SELECTOR_SPEED
            selector[Transform].scale = SELECTOR_SCALE
            selector[Graphic].color.set(1f, 1f, 1f, 1f)
            selectEntityIterator.goToFirst { it == activeSelector[Selector].target }
        }
    }

    fun stopOrRevertSelection(): Boolean {
        if (targetType == ActionTargetType.MULTI && selectorEntities.numEntities > 1) {
            // at least one target was selected -> revert it
            world -= activeSelector
            activeSelector = selectorEntities.first()
            world.confirmSelector(activeSelector, false)
            playSndMenuAbort()
            return false
        }

        selectorEntities.forEach { it.remove() }
        activeSelector = Entity.NONE
        playSndMenuAbort()
        return true
    }

    private fun spawnNextMultiSelector() = with(world) {
        // get first target that has no selector entity linked to it yet
        val target = selectEntityIterator.goToFirst { enemy -> selectorEntities.none { it[Selector].target == enemy } }
        activeSelector = selectorEntity(target, false)
        playSndMenuAccept()
    }

    private fun Entity.alreadyConfirmedTarget(target: Entity): Boolean = with(world) {
        return selectorEntities.any { it != this@alreadyConfirmedTarget && it[Selector].target == target }
    }

    fun confirmTargetSelection(): Boolean = with(world) {
        if (targetType == ActionTargetType.MULTI) {
            // multi target selection and selected target was not confirmed yet -> confirm it
            val selectorCmp = activeSelector[Selector]
            if (activeSelector.alreadyConfirmedTarget(selectorCmp.target)) {
                // multi select target was already confirmed before -> start action with selected targets
                activeSelector.remove()
                setActionTargets()
                return@with true
            }

            // target was not selected before -> confirm it
            world.confirmSelector(activeSelector, true)
            if (selectorEntities.numEntities < enemyEntities.size) {
                // not all targets selected yet
                spawnNextMultiSelector()
                return@with false
            }
        }

        setActionTargets()
        return@with true
    }

    private fun setActionTargets() = with(world) {
        log.debug { "Selected targets: ${selectorEntities.entities.map { it[Selector].target }}" }

        // play sound effect
        playSndMenuAccept()
        // remove selector entities and update player action's targets
        val player = playerEntities.single()
        val combat = player[Combat]
        combat.targets.clear()
        selectorEntities.forEach { selector ->
            combat.targets += selector[Selector].target
            world -= selector
        }
        // fire event to trigger CombatSystem and start round
        eventService.fire(CombatPlayerActionEvent(player))
    }

    companion object {
        private val log = logger<CombatViewModel>()
    }

}
