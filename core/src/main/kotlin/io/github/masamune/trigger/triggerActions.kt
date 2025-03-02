package io.github.masamune.trigger

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.MutableEntityBag
import io.github.masamune.Masamune
import io.github.masamune.addItem
import io.github.masamune.asset.MusicAsset
import io.github.masamune.asset.SoundAsset
import io.github.masamune.asset.TiledMapAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.component.Animation
import io.github.masamune.component.CharacterStats
import io.github.masamune.component.Facing
import io.github.masamune.component.FacingDirection
import io.github.masamune.component.FollowPath
import io.github.masamune.component.Graphic
import io.github.masamune.component.Inventory
import io.github.masamune.component.Move
import io.github.masamune.component.MoveTo
import io.github.masamune.component.Name
import io.github.masamune.component.QuestLog
import io.github.masamune.component.Tag
import io.github.masamune.component.Transform
import io.github.masamune.dialog.DialogConfigurator
import io.github.masamune.event.CutSceneTextEvent
import io.github.masamune.event.DialogBeginEvent
import io.github.masamune.event.EventService
import io.github.masamune.event.PlayerQuestItemBegin
import io.github.masamune.event.PlayerQuestItemEnd
import io.github.masamune.event.ShopBeginEvent
import io.github.masamune.quest.Quest
import io.github.masamune.removeItem
import io.github.masamune.scheduledTask
import io.github.masamune.spawnSfx
import io.github.masamune.tiledmap.AnimationType
import io.github.masamune.tiledmap.ItemType
import io.github.masamune.tiledmap.TiledService
import io.github.masamune.trigger.actions.EntitySelector
import io.github.masamune.ui.model.I18NKey
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2

sealed interface TriggerAction {
    fun World.onStart() = Unit

    fun World.onUpdate(): Boolean
}

data class TriggerActionRemoveEntity(val entity: Entity) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        if (entity == Entity.NONE || entity !in this) {
            // entity already removed or not found (e.g. getEntityByTiledId returns Entity.NONE)
            return true
        }

        entity.remove()
        return true
    }
}

class TriggerActionDialog(
    dialogConfigurator: DialogConfigurator,
    dialogName: String,
    world: World,
    val triggeringEntity: Entity,
    val eventService: EventService,
    val closeAction: (selectedOptionIdx: Int) -> Unit
) : TriggerAction {
    private val namedDialog = dialogConfigurator[dialogName, world, triggeringEntity]

    override fun World.onStart() {
        eventService.fire(DialogBeginEvent(this, triggeringEntity, namedDialog))
    }

    override fun World.onUpdate(): Boolean {
        if (namedDialog.isFinished) {
            if (closeAction != NO_CLOSE_ACTION) {
                closeAction(namedDialog.lastOptionIdx)
            }
            return true
        }
        return false
    }

    companion object {
        val NO_CLOSE_ACTION: (Int) -> Unit = {}
    }
}

class TriggerActionAddItem(
    private val entity: Entity,
    private val itemType: ItemType,
    private val eventService: EventService,
    private val tiledService: TiledService,
    private val audioService: AudioService,
) : TriggerAction {
    private var duration = 4.5f

    override fun World.onStart() {
        val item: Entity = tiledService.loadItem(this, itemType)
        eventService.fire(PlayerQuestItemBegin(entity, item))
        addItem(item, entity, true)
        entity[Animation].changeTo = AnimationType.ITEM
        scheduledTask(0.5f) { audioService.play(SoundAsset.QUEST_ITEM) }
    }

    override fun World.onUpdate(): Boolean {
        duration -= deltaTime
        if (duration <= 0f) {
            eventService.fire(PlayerQuestItemEnd)
            entity[Animation].changeTo = AnimationType.IDLE
            return true
        }
        return false
    }
}

class TriggerActionRemoveItem(
    private val entity: Entity,
    private val itemType: ItemType,
    private val amount: Int,
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        removeItem(itemType, amount, entity, true)
        return true
    }
}

class TriggerActionAddQuest(
    private val entity: Entity,
    private val quest: Quest,
    private val eventService: EventService,
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        val quests = entity[QuestLog].quests
        if (quest in quests) {
            log.info { "Quest $quest is already part of the QuestLog" }
        } else {
            log.info { "Adding quest $quest" }
            eventService += quest
            entity[QuestLog].quests += quest
        }
        return true
    }

    companion object {
        private val log = logger<TriggerActionAddQuest>()
    }
}

class TriggerActionCompleteQuest(
    private val quest: Quest,
    private val eventService: EventService,
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        eventService -= quest
        quest.complete()
        log.info { "Completed quest $quest" }
        return true
    }

    companion object {
        private val log = logger<TriggerActionAddQuest>()
    }
}

class TriggerActionMoveBack(
    private val entity: Entity,
    private val distance: Float,
    private var durationInSeconds: Float,
    private val wait: Boolean,
) : TriggerAction {
    override fun World.onStart() {
        val (x, y) = entity[Transform].position
        var (dirX, dirY) = entity[Move].direction
        if (dirX == 0f && dirY == 0f) {
            // might happen in special scenarios -> use facing as a fallback
            when (entity[Facing].direction) {
                FacingDirection.LEFT -> dirX = -1f
                FacingDirection.RIGHT -> dirX = 1f
                FacingDirection.UP -> dirY = 1f
                else -> dirY = -1f
            }
        }

        entity.configure {
            it += MoveTo(vec2(x - distance * dirX, y - distance * dirY), durationInSeconds, Interpolation.linear)
        }
    }

    override fun World.onUpdate(): Boolean {
        durationInSeconds -= this.deltaTime
        return !wait || durationInSeconds <= 0f
    }
}

class TriggerActionShop(
    private val playerEntity: Entity,
    private val shopEntity: Entity,
    private val shopName: I18NKey,
    private val items: List<ItemType>,
    private val tiledService: TiledService,
    private val eventService: EventService = tiledService.eventService,
) : TriggerAction {
    override fun World.onStart() {
        val shopItems = MutableEntityBag(items.size)
        items.forEach { item ->
            shopItems += tiledService.loadItem(this, item)
        }
        shopEntity.configure {
            it += Name(shopName.key)
            it += Inventory(items = shopItems)
        }

        eventService.fire(ShopBeginEvent(this, playerEntity, shopEntity))
    }

    override fun World.onUpdate(): Boolean = true
}

class TriggerActionHeal(
    private val entity: Entity,
    private val healLife: Boolean,
    private val healMana: Boolean,
    private val audioService: AudioService,
) : TriggerAction {

    override fun World.onUpdate(): Boolean {
        val stats = entity[CharacterStats]
        val sfxKey = when {
            healLife && healMana -> "restore_purple"
            healLife -> "restore_green"
            else -> "restore_blue"
        }
        if (healLife) {
            stats.life = stats.lifeMax
        }
        if (healMana) {
            stats.mana = stats.manaMax
        }
        spawnSfx(entity, sfxKey, 1f, 2f)
        audioService.play(SoundAsset.HEAL1)
        return true
    }
}

class TriggerActionPauseEntity(
    private val entity: Entity,
    private val pause: Boolean,
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        if (pause) {
            entity.configure { it += Tag.PAUSE }
        } else {
            entity.configure { it -= Tag.PAUSE }
        }
        return true
    }
}

class TriggerActionPlayMusic(
    private val audioService: AudioService,
    private val music: MusicAsset,
    private val loop: Boolean = true,
    private val keepPrevious: Boolean = false,
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        audioService.play(music, loop, keepPrevious)
        return true
    }
}

class TriggerActionDelay(private var seconds: Float) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        seconds -= deltaTime
        return seconds <= 0f
    }
}

class TriggerActionChangeScreen(
    private val masamune: Masamune,
    private val action: Masamune.() -> Unit,
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        masamune.action()
        return true
    }
}

class TriggerActionCutSceneText(
    private val i18NKey: I18NKey,
    private val align: Int,
    private val duration: Float,
    private val eventService: EventService
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        eventService.fire(CutSceneTextEvent(i18NKey, align, duration))
        return true
    }
}

class TriggerActionFadeOutMusic(
    private val duration: Float,
    private val audioService: AudioService,
) : TriggerAction {

    private var initialVolumne = 0f
    private var alpha = 0f
    private lateinit var music: Music

    override fun World.onStart() {
        val intervalSeconds = 1 / 30f
        music = audioService.currentMusic
        initialVolumne = music.volume

        scheduledTask(0f, intervalSeconds) { task ->
            if (music !== audioService.currentMusic) {
                // music has changed -> cancel fade out
                task.cancel()
                return@scheduledTask
            }

            alpha += intervalSeconds * (1f / duration)
            val volume = Interpolation.linear.apply(initialVolumne, 0f, alpha.coerceAtMost(1f))
            music.volume = volume
            if (volume == 0f) {
                task.cancel()
            }
        }
    }

    override fun World.onUpdate(): Boolean = true
}

class TriggerActionLoadMap(
    private val asset: TiledMapAsset,
    private val tiledService: TiledService,
    private val withBoundaries: Boolean,
    private val withTriggers: Boolean,
    private val withPortals: Boolean,
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        val map = tiledService.loadMap(asset)
        tiledService.setMap(map, this, withBoundaries, withTriggers, withPortals)

        return true
    }
}

class TriggerActionHideEntity(private val entitySelector: EntitySelector) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        with(entitySelector) {
            selectedEntity().configure { it[Graphic].color.a = 0f }
        }
        return true
    }
}

class TriggerActionFollowPath(
    private val entitySelector: EntitySelector,
    private val pathId: Int,
    private val removeAtEnd: Boolean,
    private val waitForEnd: Boolean,
    private val tiledService: TiledService,
) : TriggerAction {

    private var entity = Entity.NONE

    override fun World.onStart() {
        val pathVertices = tiledService.loadPath(pathId)
        with(entitySelector) {
            entity = selectedEntity()
            entity.configure {
                it += FollowPath(pathVertices, removeAtEnd, 0)
            }
        }
    }

    override fun World.onUpdate(): Boolean {
        return !waitForEnd || entity hasNo FollowPath
    }
}

class TriggerActionEntitySpeed(
    private val entitySelector: EntitySelector,
    private val speed: Float,
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        with(entitySelector) {
            selectedEntity().configure { it[Move].speed = speed }
        }
        return true
    }
}
