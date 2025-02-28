package io.github.masamune.trigger

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.trigger.cutscene.intro.cutSceneIntroTrigger
import io.github.masamune.trigger.forest.entrance.forestEntranceTrigger
import io.github.masamune.trigger.forest.entrance.terealisFlowerTrigger
import io.github.masamune.trigger.forest.masamune.masamuneForestTrigger
import io.github.masamune.trigger.forest.path.manGreenTrigger
import io.github.masamune.trigger.village.elderTrigger
import io.github.masamune.trigger.village.flowerGirlTrigger
import io.github.masamune.trigger.village.merchantTrigger
import io.github.masamune.trigger.village.smithTrigger
import io.github.masamune.trigger.village.villageExitTrigger
import ktx.app.gdxError
import ktx.log.logger

class TriggerConfigurator {

    operator fun get(name: String, world: World, scriptEntity: Entity, triggeringEntity: Entity): TriggerScript {
        log.debug { "Creating new trigger $name" }

        return when (name) {
            "villageExit" -> world.villageExitTrigger(name, scriptEntity, triggeringEntity)
            "elder" -> world.elderTrigger(name, triggeringEntity)
            "merchant" -> world.merchantTrigger(name, triggeringEntity, scriptEntity)
            "smith" -> world.smithTrigger(name, triggeringEntity, scriptEntity)
            "flower_girl" -> world.flowerGirlTrigger(name, triggeringEntity)
            "terealis_flower" -> world.terealisFlowerTrigger(name, scriptEntity, triggeringEntity)
            "man_green" -> world.manGreenTrigger(name, scriptEntity, triggeringEntity)
            "cut_scene_intro" -> world.cutSceneIntroTrigger(name, scriptEntity)
            "masamune_forest" -> world.masamuneForestTrigger(name, scriptEntity, triggeringEntity)

            else -> gdxError("There is no trigger configured for name $name")
        }
    }

    operator fun get(name: String, world: World): TriggerScript? {
        log.debug { "Creating new trigger $name" }
        return when (name) {
            "forest_entrance" -> world.forestEntranceTrigger(name)
            else -> gdxError("There is no trigger configured for name $name")
        }
    }

    companion object {
        private val log = logger<TriggerConfigurator>()
    }
}

