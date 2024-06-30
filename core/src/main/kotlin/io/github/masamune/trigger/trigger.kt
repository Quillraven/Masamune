package io.github.masamune.trigger

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.dialog.DialogConfigurator
import io.github.masamune.event.DialogBeginEvent
import io.github.masamune.event.EventService

typealias TriggerScriptFactory = (world: World, scriptEntity: Entity, triggeringEntity: Entity) -> TriggerScript

sealed interface TriggerAction {
    fun World.onStart() = Unit

    fun World.onUpdate(): Boolean
}

data class TriggerActionRemoveEntity(private val entity: Entity) : TriggerAction {
    override fun World.onUpdate(): Boolean {
        entity.remove()
        return true
    }
}

class TriggerActionDialog(
    dialogConfigurator: DialogConfigurator,
    dialogName: String,
    world: World,
    private val triggeringEntity: Entity,
    private val eventService: EventService,
    private val closeAction: (selectedOptionIdx: Int) -> Unit
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

class TriggerScript(
    val world: World,
    private val actions: MutableList<TriggerAction>
) {

    init {
        actions.first().run { world.onStart() }
    }

    fun onUpdate(): Boolean {
        actions.first().run {
            if (world.onUpdate()) {
                // action finished -> go to next action or end script if there are no other actions
                actions.removeFirst()
                if (actions.isEmpty()) {
                    return true
                }
                actions.first().run { world.onStart() }
            }
        }

        return false
    }

}

enum class TriggerScriptType(val scriptFactory: TriggerScriptFactory) {
    VILLAGE_EXIT(villageExitScript)
}

private val villageExitScript: TriggerScriptFactory = { world, scriptEntity, triggeringEntity ->
    trigger(world, triggeringEntity) {
        actionRemove(scriptEntity)
        actionDialog("elder_00") { selectedOptionIdx ->
            println("$selectedOptionIdx")
        }
    }
}
