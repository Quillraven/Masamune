package io.github.masamune.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.masamune.component.Tag
import io.github.masamune.component.Trigger
import io.github.masamune.event.Event
import io.github.masamune.event.EventListener
import io.github.masamune.event.MapChangeEvent
import io.github.masamune.trigger.TriggerConfigurator
import io.github.masamune.trigger.TriggerScript
import ktx.log.logger
import ktx.tiled.property

class TriggerSystem(
    private val triggerConfigurator: TriggerConfigurator = inject(),
) : IteratingSystem(family { all(Trigger, Tag.EXECUTE_TRIGGER) }), EventListener {

    private val activeTriggers = mutableListOf<TriggerScript>()
    private var lastTriggerEntity = Entity.NONE

    override fun onTick() {
        super.onTick()

        // run active triggers
        if (activeTriggers.isNotEmpty()) {
            val triggerIterator = activeTriggers.iterator()
            while (triggerIterator.hasNext()) {
                val script = triggerIterator.next()
                if (script.onUpdate()) {
                    // script finished -> remove it
                    triggerIterator.remove()
                    log.debug { "Finished trigger ${script.name}. Remaining active triggers: ${activeTriggers.size}" }
                }
            }
            return
        }

        lastTriggerEntity = Entity.NONE
    }

    // this method only gets called for entities that have a trigger that just got triggered (=Tag.EXECUTE_TRIGGER)
    override fun onTickEntity(entity: Entity) {
        val (triggerName, triggeringEntity) = entity[Trigger]
        entity.configure { it -= Tag.EXECUTE_TRIGGER }
        if (lastTriggerEntity == entity) {
            return
        }

        val script = triggerConfigurator[triggerName, world, entity, triggeringEntity]
        activeTriggers += script
        lastTriggerEntity = entity
    }

    override fun onEvent(event: Event) {
        if (event !is MapChangeEvent) {
            return
        }

        val tiledTriggerName = event.tiledMap.property("trigger", "")
        if (tiledTriggerName.isNotBlank()) {
            triggerConfigurator[tiledTriggerName, world]?.let { mapTrigger ->
                activeTriggers += mapTrigger
            }
        }
    }

    companion object {
        private val log = logger<TriggerSystem>()
    }
}
