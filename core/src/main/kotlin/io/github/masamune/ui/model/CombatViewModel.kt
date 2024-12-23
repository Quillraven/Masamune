package io.github.masamune.ui.model

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.compareEntityBy
import com.github.quillraven.fleks.collection.mutableEntityBagOf
import io.github.masamune.asset.CachingAtlas
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.action.ActionTargetType
import io.github.masamune.component.Animation
import io.github.masamune.component.Combat
import io.github.masamune.component.Graphic
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Selector
import io.github.masamune.component.Stats
import io.github.masamune.component.Transform
import io.github.masamune.event.CombatEntityManaUpdateEvent
import io.github.masamune.event.CombatEntityTakeDamageEvent
import io.github.masamune.event.CombatNextTurnEvent
import io.github.masamune.event.CombatPlayerActionEvent
import io.github.masamune.event.CombatStartEvent
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.tiledmap.AnimationType
import ktx.log.logger
import ktx.math.vec2

class CombatViewModel(
    bundle: I18NBundle,
    audioService: AudioService,
    private val world: World,
    private val eventService: EventService,
    private val gameViewport: Viewport,
    private val uiViewport: Viewport,
    private val charPropAtlas: CachingAtlas,
) : ViewModel(bundle, audioService) {

    private val playerEntities = world.family { all(Player, Combat) }

    // Separate enemy bag because we sort them by x coordinate which makes it more intuitive for target selection.
    // The left most enemy is the first target while the right most entity is the last target.
    // The normal enemy sorting is by agility which does not make sense for target selection.
    private val enemyEntities = mutableEntityBagOf()
    private val enemyComparator = compareEntityBy(Transform, world)
    private val selectorEntities = world.family { all(Selector) }
    private var targetType = ActionTargetType.NONE
    private var activeSelector = Entity.NONE

    // view attributes
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
                    MagicModel(
                        it.type,
                        bundle["magic.${it.type.name.lowercase()}.name"],
                        bundle["magic.target.${it.targetType.name.lowercase()}"],
                        it.manaCost
                    )
                }

                // position UI elements according to player position
                onResize()

                // store enemy entities for easier target selection
                enemyEntities.clear()
                enemyEntities += event.enemies
                enemyEntities.sort(enemyComparator)
            }

            is CombatNextTurnEvent -> {
                enemyEntities.clear()
                enemyEntities += event.enemies
                enemyEntities.sort(enemyComparator)
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
        // update player position to also trigger an update of view element positions in CombatView
        playerEntities.singleOrNull()?.let { player ->
            val playerPos = player[Transform].position
            playerPosition.set(playerPos.x, playerPos.y).toUiPosition(gameViewport, uiViewport)
            notify(CombatViewModel::playerPosition, playerPosition)
        }
    }

    private fun World.spawnSelectorEntity(target: Entity, targetIdx: Int, confirmed: Boolean): Entity = with(world) {
        val targetTransform = target[Transform]
        this.entity {
            it += Transform(targetTransform.position.cpy(), targetTransform.size.cpy(), 1.2f)
            val animationCmp = Animation.ofAtlas(charPropAtlas, "select", AnimationType.IDLE, speed = SELECTOR_SPEED)
            it += animationCmp
            it += Graphic(animationCmp.gdxAnimation.getKeyFrame(0f))
            it += Selector(target, targetIdx, confirmed)
        }
    }

    private fun spawnTargetSelectorEntities() = with(world) {
        when (targetType) {
            ActionTargetType.SINGLE -> {
                activeSelector = spawnSelectorEntity(enemyEntities.first(), 0, true)
            }

            ActionTargetType.MULTI -> {
                activeSelector = spawnSelectorEntity(enemyEntities.first(), 0, false)
            }

            ActionTargetType.ALL -> {
                enemyEntities.forEachIndexed { idx, entity ->
                    spawnSelectorEntity(entity, idx, true)
                }
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
        this@CombatViewModel.playSndMenuAccept()
    }

    private fun updateSelection() = with(world) {
        log.debug { "Update selection to: ${selectorEntities.map { it[Selector].targetIdx }}" }

        val selectorCmp = activeSelector[Selector]
        val target = enemyEntities[selectorCmp.targetIdx]
        val (targetPos, targetSize) = target[Transform]
        val selectorTransform = activeSelector[Transform]
        selectorTransform.position.set(targetPos)
        selectorTransform.size.set(targetSize)
        selectorCmp.target = target
    }

    fun selectPrevTarget() = with(world) {
        if (targetType == ActionTargetType.SINGLE || targetType == ActionTargetType.MULTI) {
            val selectorCmp = activeSelector[Selector]
            if (selectorCmp.targetIdx == 0) {
                selectorCmp.targetIdx = enemyEntities.size - 1
            } else {
                selectorCmp.targetIdx = selectorCmp.targetIdx - 1
            }

            updateSelection()
            this@CombatViewModel.playSndMenuClick()
        }
    }

    fun selectNextTarget() = with(world) {
        if (targetType == ActionTargetType.SINGLE || targetType == ActionTargetType.MULTI) {
            val selectorCmp = activeSelector[Selector]
            if (selectorCmp.targetIdx == enemyEntities.size - 1) {
                selectorCmp.targetIdx = 0
            } else {
                selectorCmp.targetIdx = selectorCmp.targetIdx + 1
            }

            updateSelection()
            this@CombatViewModel.playSndMenuClick()
        }
    }

    fun stopOrRevertSelection(): Boolean {
        if (targetType == ActionTargetType.MULTI && selectorEntities.numEntities > 1) {
            // at least one target was selected -> revert it
            world -= activeSelector
            activeSelector = selectorEntities.first()
            with(world) {
                activeSelector[Selector].confirmed = false
                activeSelector[Animation].speed = SELECTOR_SPEED
            }
            return false
        }

        selectorEntities.forEach { selector ->
            world -= selector
        }
        activeSelector = Entity.NONE
        playSndMenuAbort()
        return true
    }

    private fun spawnNextMultiSelector() = with(world) {
        enemyEntities.forEachIndexed { idx, enemy ->
            if (selectorEntities.none { it[Selector].target == enemy }) {
                activeSelector = spawnSelectorEntity(enemy, idx, false)
                this@CombatViewModel.playSndMenuAccept()
                return@with
            }
        }
    }

    fun confirmTargetSelection(): Boolean = with(world) {
        if (targetType == ActionTargetType.MULTI && activeSelector[Selector].confirmed == false) {
            activeSelector[Selector].confirmed = true
            activeSelector[Animation].run {
                speed = 0f
                stateTime = 0f
            }
            if (selectorEntities.numEntities < enemyEntities.size) {
                // not all targets selected yet
                spawnNextMultiSelector()
                return@with false
            }
        }

        log.debug { "Selected targets: $selectorEntities" }

        // play sound effect
        this@CombatViewModel.playSndMenuAccept()
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

        return@with true
    }

    companion object {
        private val log = logger<CombatViewModel>()
        private const val SELECTOR_SPEED = 1.5f

        private fun Vector2.toUiPosition(from: Viewport, to: Viewport): Vector2 {
            from.project(this)
            to.unproject(this)
            this.y = to.worldHeight - this.y
            return this
        }
    }

}
