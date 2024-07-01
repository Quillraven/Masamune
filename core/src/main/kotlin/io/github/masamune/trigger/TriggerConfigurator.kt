package io.github.masamune.trigger

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import ktx.app.gdxError
import ktx.log.logger

class TriggerConfigurator {

    operator fun get(name: String, world: World, scriptEntity: Entity, triggeringEntity: Entity): TriggerScript {
        log.debug { "Creating new trigger $name" }

        return when (name) {
            "villageExit" -> world.villageExitTrigger(name, scriptEntity, triggeringEntity)
            "elder" -> world.elderTrigger(name, triggeringEntity)

            else -> gdxError("There is no trigger configured for name $name")
        }
    }

    private fun World.villageExitTrigger(name: String, scriptEntity: Entity, triggeringEntity: Entity) =
        trigger(name, this, triggeringEntity) {
            actionRemove(scriptEntity)
            actionDialog("elder_00") { selectedOptionIdx ->
                println("$selectedOptionIdx")
            }
        }

    private fun World.elderTrigger(name: String, triggeringEntity: Entity) =
        trigger(name, this, triggeringEntity) {
            actionDialog("elder_00")
        }

    companion object {
        private val log = logger<TriggerConfigurator>()
    }
}
