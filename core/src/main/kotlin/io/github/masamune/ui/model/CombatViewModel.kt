package io.github.masamune.ui.model

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.EntityBagIterator
import com.github.quillraven.fleks.collection.MutableEntityBag
import com.github.quillraven.fleks.collection.compareEntity
import com.github.quillraven.fleks.collection.iterator
import io.github.masamune.asset.CachingAtlas
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.action.Action
import io.github.masamune.combat.action.ActionTargetType
import io.github.masamune.component.Animation
import io.github.masamune.component.Combat
import io.github.masamune.component.Graphic
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Selector
import io.github.masamune.component.Stats
import io.github.masamune.component.Transform
import io.github.masamune.event.CombatEntityHealEvent
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
import ktx.math.vec3

class CombatViewModel(
    bundle: I18NBundle,
    audioService: AudioService,
    private val world: World,
    private val eventService: EventService,
    private val gameViewport: Viewport,
    private val uiViewport: Viewport,
    private val charPropAtlas: CachingAtlas,
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
    private lateinit var selectEntityIterator: EntityBagIterator
    private var activeSelector = Entity.NONE

    // view attributes
    var playerLife: Pair<Float, Float> by propertyNotify(0f to 0f)
    var playerMana: Pair<Float, Float> by propertyNotify(0f to 0f)
    var playerName: String by propertyNotify("")
    var playerPosition: Vector2 by propertyNotify(vec2())
    var playerMagic: List<MagicModel> by propertyNotify(emptyList<MagicModel>())
    var combatTurn: Int by propertyNotify(-1)

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
                enemyEntities.clear()
                enemyEntities += event.enemies
                enemyEntities.sort(targetComparator)
                playerEntities.clear()
                playerEntities += event.player
                playerEntities.sort(targetComparator)
                ++combatTurn
            }

            is CombatEntityTakeDamageEvent -> {
                if (event.entity hasNo Player) {
                    return@with
                }

                playerLife = event.life to event.maxLife
            }

            is CombatEntityHealEvent -> {
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

    private fun World.spawnSelectorEntity(target: Entity, confirmed: Boolean): Entity = this.entity {
        // position and size don't matter because they get updated in the SelectorSystem
        it += Transform(vec3(), vec2(), SELECTOR_SCALE)
        val animationCmp = Animation.ofAtlas(charPropAtlas, "select", AnimationType.IDLE, speed = SELECTOR_SPEED)
        it += animationCmp
        it += Graphic(animationCmp.gdxAnimation.getKeyFrame(0f))
        it += Selector(target, confirmed)
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
                activeSelector = spawnSelectorEntity(selectEntityIterator.next(), true)
            }

            ActionTargetType.MULTI -> {
                activeSelector = spawnSelectorEntity(selectEntityIterator.next(), false)
            }

            ActionTargetType.ALL -> {
                while (selectEntityIterator.hasNext()) {
                    spawnSelectorEntity(selectEntityIterator.next(), true)
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
        activeSelector = spawnSelectorEntity(target, false)
        playSndMenuAccept()
    }

    fun confirmTargetSelection(): Boolean = with(world) {
        if (targetType == ActionTargetType.MULTI && activeSelector[Selector].confirmed == false) {
            // multi target selection and selected target was not confirmed yet -> confirm it
            world.confirmSelector(activeSelector, true)
            if (selectorEntities.numEntities < enemyEntities.size) {
                // not all targets selected yet
                spawnNextMultiSelector()
                return@with false
            }
        }

        log.debug { "Selected targets: $selectorEntities" }

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

        return@with true
    }

    companion object {
        private val log = logger<CombatViewModel>()
        private const val SELECTOR_SPEED = 1.5f
        private const val SELECTOR_SCALE = 1.2f

        private fun Vector2.toUiPosition(from: Viewport, to: Viewport): Vector2 {
            from.project(this)
            to.unproject(this)
            this.y = to.worldHeight - this.y
            return this
        }
    }

}
