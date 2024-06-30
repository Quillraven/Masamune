package io.github.masamune.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.masamune.component.Tag
import io.github.masamune.component.Trigger
import io.github.masamune.trigger.TriggerScript
import io.github.masamune.trigger.TriggerScriptType
import ktx.app.gdxError
import ktx.log.logger

class TriggerSystem : IteratingSystem(family { all(Trigger, Tag.EXECUTE_TRIGGER) }) {

    private val activeTriggers = mutableMapOf<TriggerScriptType, TriggerScript>()
    private val finishedTriggers = mutableListOf<TriggerScriptType>()

    override fun onTick() {
        super.onTick()
        activeTriggers.forEach { (triggerType, triggerScript) ->
            if (triggerScript.onUpdate()) {
                // script finished -> remove it
                finishedTriggers += triggerType
            }
        }
        finishedTriggers.forEach {
            log.debug { "Removing trigger $it" }
            activeTriggers -= it
        }
        finishedTriggers.clear()
    }

    // this method only gets called for entities that have a trigger that just got triggered
    override fun onTickEntity(entity: Entity) {
        val (triggerName, triggeringEntity) = entity[Trigger]
        entity.configure { it -= Tag.EXECUTE_TRIGGER }

        val scriptType = TriggerScriptType.entries.firstOrNull { it.name == triggerName }
            ?: gdxError("There is no trigger for name $triggerName")
        if (scriptType !in activeTriggers) {
            // script type not active yet -> create it
            log.debug { "Creating trigger $scriptType" }
            val script = scriptType.scriptFactory(world, entity, triggeringEntity)
            activeTriggers[scriptType] = script
        }
    }

    companion object {
        private val log = logger<TriggerSystem>()
    }
}
