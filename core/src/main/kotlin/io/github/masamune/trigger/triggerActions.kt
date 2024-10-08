package io.github.masamune.trigger

import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Facing
import io.github.masamune.component.FacingDirection
import io.github.masamune.component.Inventory
import io.github.masamune.component.Move
import io.github.masamune.component.MoveTo
import io.github.masamune.component.QuestLog
import io.github.masamune.component.Transform
import io.github.masamune.dialog.DialogConfigurator
import io.github.masamune.event.DialogBeginEvent
import io.github.masamune.event.EventService
import io.github.masamune.quest.Quest
import io.github.masamune.tiledmap.ItemType
import io.github.masamune.tiledmap.TiledService
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
    private val tiledService: TiledService
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        val item: Entity = tiledService.loadItem(this, itemType)
        entity[Inventory].items += item
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
