package io.github.masamune.trigger

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Inventory
import io.github.masamune.component.QuestLog
import io.github.masamune.dialog.DialogConfigurator
import io.github.masamune.event.DialogBeginEvent
import io.github.masamune.event.EventService
import io.github.masamune.quest.Quest
import io.github.masamune.tiledmap.TiledService
import ktx.log.logger

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
    private val itemName: String,
    private val tiledService: TiledService
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        val item: Entity = tiledService.loadItem(this, itemName)
        entity[Inventory].items += item
        return true
    }
}

class TriggerActionAddQuest(
    private val entity: Entity,
    private val quest: Quest,
) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        val quests = entity[QuestLog].quests
        if (quest in quests) {
            log.info { "Quest $quest is already part of the QuestLog" }
        } else {
            log.info { "Adding quest $quest" }
            entity[QuestLog].quests += quest
        }
        return true
    }

    companion object {
        private val log = logger<TriggerActionAddQuest>()
    }
}
