package io.github.masamune.trigger

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.QuestLog
import io.github.masamune.quest.FlowerGirlQuest
import io.github.masamune.quest.MainQuest
import io.github.masamune.tiledmap.ItemType
import ktx.app.gdxError
import ktx.log.logger

class TriggerConfigurator {

    operator fun get(name: String, world: World, scriptEntity: Entity, triggeringEntity: Entity): TriggerScript {
        log.debug { "Creating new trigger $name" }

        return when (name) {
            "villageExit" -> world.villageExitTrigger(name, scriptEntity, triggeringEntity)
            "elder" -> world.elderTrigger(name, triggeringEntity)
            "merchant" -> world.merchantTrigger(name, triggeringEntity)
            "smith" -> world.smithTrigger(name, triggeringEntity)
            "flower_girl" -> world.flowerGirlTrigger(name, triggeringEntity)

            else -> gdxError("There is no trigger configured for name $name")
        }
    }

    private fun World.villageExitTrigger(name: String, scriptEntity: Entity, triggeringEntity: Entity): TriggerScript {
        val mainQuest = triggeringEntity[QuestLog].getOrNull<MainQuest>()

        return when (mainQuest) {
            null -> trigger(name, this, triggeringEntity) {
                // push player away to not trigger the same map trigger again and again
                actionMoveBack(triggeringEntity, distance = 0.75f, timeInSeconds = 0.25f, wait = true)
                actionDialog("villageExit")
            }

            else -> trigger(name, this, triggeringEntity) {
                actionRemove(scriptEntity)
            }
        }
    }


    private fun World.elderTrigger(name: String, triggeringEntity: Entity): TriggerScript {
        val mainQuest = triggeringEntity[QuestLog].getOrNull<MainQuest>()

        return when (mainQuest) {
            null -> {
                trigger(name, this, triggeringEntity) {
                    actionDialog("elder_00")
                    actionAddItem(triggeringEntity, ItemType.ELDER_SWORD)
                    actionAddQuest(triggeringEntity, MainQuest())
                }
            }

            else -> {
                trigger(name, this, triggeringEntity) {
                    actionDialog("elder_10")
                }
            }
        }
    }

    private fun World.merchantTrigger(name: String, triggeringEntity: Entity) =
        trigger(name, this, triggeringEntity) {
            actionDialog("merchant_00") { selectedOptionIdx ->
                if (selectedOptionIdx == 0) {
                    println("TODO open shop merchant UI")
                }
            }
        }

    private fun World.smithTrigger(name: String, triggeringEntity: Entity) =
        trigger(name, this, triggeringEntity) {
            actionDialog("smith_00") { selectedOptionIdx ->
                if (selectedOptionIdx == 0) {
                    println("TODO open shop smith UI")
                }
            }
        }

    private fun World.flowerGirlTrigger(name: String, triggeringEntity: Entity): TriggerScript {
        val flowerGirlQuest = triggeringEntity[QuestLog].getOrNull<FlowerGirlQuest>()

        return when (flowerGirlQuest) {
            null -> trigger(name, this, triggeringEntity) {
                actionDialog("flower_girl_00") { selectedOptionIdx ->
                    if (selectedOptionIdx == 0) {
                        println("TODO add flower quest")
                    }
                }
                actionAddQuest(triggeringEntity, FlowerGirlQuest())
            }

            else -> trigger(name, this, triggeringEntity) {
                actionDialog("flower_girl_10")
            }
        }
    }

    companion object {
        private val log = logger<TriggerConfigurator>()
    }
}

