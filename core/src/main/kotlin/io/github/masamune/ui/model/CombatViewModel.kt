package io.github.masamune.ui.model

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.compareEntityBy
import com.github.quillraven.fleks.collection.mutableEntityBagOf
import io.github.masamune.asset.CachingAtlas
import io.github.masamune.combat.action.ActionTargetType
import io.github.masamune.component.Animation
import io.github.masamune.component.Combat
import io.github.masamune.component.Graphic
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.component.Tag
import io.github.masamune.component.Transform
import io.github.masamune.event.CombatEntityManaUpdateEvent
import io.github.masamune.event.CombatEntityTakeDamageEvent
import io.github.masamune.event.CombatNextTurnEvent
import io.github.masamune.event.CombatPlayerActionEvent
import io.github.masamune.event.CombatStartEvent
import io.github.masamune.event.DialogBackEvent
import io.github.masamune.event.DialogOptionChangeEvent
import io.github.masamune.event.DialogOptionTriggerEvent
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.tiledmap.AnimationType
import ktx.log.logger
import ktx.math.vec2
import ktx.math.vec3

class CombatViewModel(
    bundle: I18NBundle,
    private val world: World,
    private val eventService: EventService,
    private val gameViewport: Viewport,
    private val uiViewport: Viewport,
    private val charsAndPropsAtlas: CachingAtlas,
) : ViewModel(bundle) {

    private val playerEntities = world.family { all(Player, Combat) }
    private val enemyEntities = mutableEntityBagOf()
    private val selectorEntities = world.family { all(Tag.COMBAT_SELECTOR) }
    private var targetType = ActionTargetType.NONE
    private var targetSelected = mutableListOf<Boolean>()

    var playerLife: Pair<Float, Float> by propertyNotify(0f to 0f)
    var playerMana: Pair<Float, Float> by propertyNotify(0f to 0f)
    var playerName: String by propertyNotify("")
    var playerPosition: Vector2 by propertyNotify(vec2())
    var playerMagic: List<MagicModel> by propertyNotify(emptyList<MagicModel>())

    override fun onEvent(event: Event) = with(world) {
        when (event) {
            is CombatStartEvent -> {
                val player = event.player
                // get player stats (life, mana, ...)
                val playerStats = player[Stats]
                playerLife = playerStats.life to playerStats.lifeMax
                playerMana = playerStats.mana to playerStats.manaMax
                playerName = player[Name].name

                // get player magic
                playerMagic = player[Combat].magicActions.map {
                    MagicModel(it.type, bundle["magic.${it.type.name.lowercase()}.name"], it.manaCost)
                }

                // position UI elements according to player position
                onResize()

                // store enemy entities for easier target selection
                enemyEntities.clear()
                enemyEntities += event.enemies
                enemyEntities.sort(compareEntityBy(Transform, world))
                targetSelected.clear()
                enemyEntities.forEach { targetSelected += false }
            }

            is CombatNextTurnEvent -> {
                enemyEntities.clear()
                enemyEntities += event.enemies
                enemyEntities.sort(compareEntityBy(Transform, world))
                targetSelected.clear()
                enemyEntities.forEach { targetSelected += false }
            }

            is CombatEntityTakeDamageEvent -> {
                if (event.entity hasNo Player) {
                    return@with
                }

                playerLife = event.life to event.maxLife
            }

            is CombatEntityManaUpdateEvent -> {
                if (event.entity hasNo Player) {
                    return@with
                }

                playerMana = event.mana to event.maxMana
            }

            else -> Unit
        }
    }

    fun onResize() = with(world) {
        playerEntities.singleOrNull()?.let { player ->
            val playerPos = player[Transform].position
            playerPosition.set(playerPos.x, playerPos.y).toUiPosition(gameViewport, uiViewport)
            notify(CombatViewModel::playerPosition, playerPosition)
        }
    }

    fun optionChanged() {
        // this triggers a sound effect
        eventService.fire(DialogOptionChangeEvent)
    }

    fun optionSelected() {
        // this triggers a sound effect
        eventService.fire(DialogOptionTriggerEvent)
    }

    fun optionCancelled() {
        // this triggers a sound effect
        eventService.fire(DialogBackEvent)
    }

    private fun World.spawnSelectorEntity(targetTransform: Transform) {
        this.entity {
            it += Transform(targetTransform.position.cpy(), targetTransform.size.cpy(), 1.2f)
            val animationCmp = Animation.ofAtlas(charsAndPropsAtlas, "select", AnimationType.IDLE, speed = 1.5f)
            it += animationCmp
            it += Graphic(animationCmp.gdxAnimation.getKeyFrame(0f))
            it += Tag.COMBAT_SELECTOR
        }
    }

    private fun spawnTargetSelectorEntities() = with(world) {
        when (targetType) {
            ActionTargetType.SINGLE -> {
                targetSelected[0] = true
                spawnSelectorEntity(enemyEntities.first()[Transform])
            }

            ActionTargetType.ALL -> {
                enemyEntities.forEachIndexed { idx, entity ->
                    targetSelected[idx] = true
                    spawnSelectorEntity(entity[Transform])
                }
            }

            ActionTargetType.MULTI -> {

            }

            ActionTargetType.NONE -> Unit
        }
    }

    fun selectAttack() = with(world) {
        val player = playerEntities.single()
        val combatCmp = player[Combat]
        combatCmp.action = combatCmp.attackAction
        targetType = combatCmp.action.targetType
        spawnTargetSelectorEntities()
    }

    fun selectMagic(magicModel: MagicModel) = with(world) {
        val player = playerEntities.single()
        val combatCmp = player[Combat]
        combatCmp.action = combatCmp.magicActions.single { it.type == magicModel.type }
        targetType = combatCmp.action.targetType
        spawnTargetSelectorEntities()
        optionSelected()
    }

    private fun updateSelection() = with(world) {
        log.debug { "Update selection to: $targetSelected" }

        if (selectorEntities.numEntities != targetSelected.count { it == true }) {
            selectorEntities.forEach { selector ->
                world -= selector
            }
            entity {
                it += Transform(vec3(1f, 1f, 0f), vec2(1f, 1f), 1.2f)
                val animationCmp = Animation.ofAtlas(charsAndPropsAtlas, "select", AnimationType.IDLE, speed = 1.5f)
                it += animationCmp
                it += Graphic(animationCmp.gdxAnimation.getKeyFrame(0f))
                it += Tag.COMBAT_SELECTOR
            }
        }

        enemyEntities
            .filterIndexed { idx, _ -> targetSelected[idx] }
            .forEach { target ->
                val selector = selectorEntities.single()
                val (targetPos, targetSize) = target[Transform]
                val selectorTransform = selector[Transform]
                selectorTransform.position.set(targetPos)
                selectorTransform.size.set(targetSize)
            }

    }

    fun selectPrevTarget() {
        if (targetType == ActionTargetType.SINGLE) {
            val selectedIdx = targetSelected.indexOfFirst { it == true }
            targetSelected[selectedIdx] = false
            if (selectedIdx == 0) {
                targetSelected[enemyEntities.size - 1] = true
            } else {
                targetSelected[selectedIdx - 1] = true
            }

            updateSelection()
            optionChanged()
        }
    }

    fun selectNextTarget() {
        if (targetType == ActionTargetType.SINGLE) {
            val selectedIdx = targetSelected.indexOfFirst { it == true }
            targetSelected[selectedIdx] = false
            if (selectedIdx == enemyEntities.size - 1) {
                targetSelected[0] = true
            } else {
                targetSelected[selectedIdx + 1] = true
            }

            updateSelection()
            optionChanged()
        }
    }

    fun stopSelection() {
        selectorEntities.forEach { selector ->
            world -= selector
        }
        optionCancelled()
    }

    fun confirmTargetSelection() = with(world) {
        log.debug { "Selected targets: $targetSelected" }

        // play sound effect
        optionSelected()
        // remove selector entities
        selectorEntities.forEach { selector ->
            world -= selector
        }
        // update player action's targets
        val player = playerEntities.single()
        val combat = player[Combat]
        combat.targets.clear()
        combat.targets += enemyEntities.filterIndexed { idx, _ -> targetSelected[idx] }
        // fire event to trigger CombatSystem and start round
        eventService.fire(CombatPlayerActionEvent(player))
    }

    companion object {
        private val log = logger<CombatViewModel>()

        private fun Vector2.toUiPosition(from: Viewport, to: Viewport): Vector2 {
            from.project(this)
            to.unproject(this)
            this.y = to.worldHeight - this.y
            return this
        }
    }

}
