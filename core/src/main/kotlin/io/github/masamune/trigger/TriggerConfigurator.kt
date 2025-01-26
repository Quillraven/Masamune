package io.github.masamune.trigger

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Player
import io.github.masamune.component.QuestLog
import io.github.masamune.getEntityByTiledId
import io.github.masamune.hasItem
import io.github.masamune.hasQuest
import io.github.masamune.quest.FlowerGirlQuest
import io.github.masamune.quest.MainQuest
import io.github.masamune.tiledmap.ItemType
import io.github.masamune.ui.model.I18NKey
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
        return when {
            hasQuest<MainQuest>(triggeringEntity) -> {
                trigger(name, this, triggeringEntity) {
                    actionDialog("elder_10")
                }
            }

            else -> {
                trigger(name, this, triggeringEntity) {
                    actionDialog("elder_00")
                    actionAddItem(triggeringEntity, ItemType.ELDER_SWORD)
                    actionAddQuest(triggeringEntity, MainQuest())
                }
            }
        }
    }

    private fun World.merchantTrigger(name: String, triggeringEntity: Entity, scriptEntity: Entity) =
        trigger(name, this, triggeringEntity) {
            actionDialog("merchant_00") { selectedOptionIdx ->
                if (selectedOptionIdx == 0) {
                    actionShop(
                        triggeringEntity, scriptEntity, I18NKey.NPC_MERCHANT_TITLE, listOf(
                            ItemType.SMALL_MANA_POTION,
                            ItemType.SMALL_HEALTH_POTION,
                            ItemType.SCROLL_INFERNO,
                        )
                    )
                }
            }
        }

    private fun World.smithTrigger(name: String, triggeringEntity: Entity, scriptEntity: Entity) =
        trigger(name, this, triggeringEntity) {
            actionDialog("smith_00") { selectedOptionIdx ->
                if (selectedOptionIdx == 0) {
                    actionShop(
                        triggeringEntity, scriptEntity, I18NKey.NPC_SMITH_TITLE, listOf(
                            ItemType.BOOTS,
                            ItemType.HELMET,
                        )
                    )
                }
            }
        }

    private fun World.flowerGirlTrigger(name: String, triggeringEntity: Entity): TriggerScript {
        val quest = triggeringEntity[QuestLog].getOrNull<FlowerGirlQuest>()
        val hasTerealisFlower = hasItem(triggeringEntity, ItemType.TEREALIS_FLOWER)

        return when {
            quest != null && !quest.isCompleted() && hasTerealisFlower -> trigger(name, this, triggeringEntity) {
                // quest completed -> give reward
                actionDialog("flower_girl_20")
                actionAddItem(triggeringEntity, ItemType.SMALL_STRENGTH_POTION)
                actionRemoveItem(triggeringEntity, ItemType.TEREALIS_FLOWER, 1)
                actionCompleteQuest(quest)
                actionHeal(triggeringEntity, healLife = true, healMana = true)
            }

            quest != null && quest.isCompleted() -> trigger(name, this, triggeringEntity) {
                // quest completed and reward already given
                actionDialog("flower_girl_30")
                actionHeal(triggeringEntity, healLife = true, healMana = true)
            }

            quest != null -> trigger(name, this, triggeringEntity) {
                // quest started but not completed yet
                actionDialog("flower_girl_10")
                actionHeal(triggeringEntity, healLife = true, healMana = true)
            }


            else -> trigger(name, this, triggeringEntity) {
                // first interaction -> no quest yet
                actionDialog("flower_girl_00") { selectedOptionIdx ->
                    if (selectedOptionIdx == 0) {
                        actionAddQuest(triggeringEntity, FlowerGirlQuest())
                        actionHeal(triggeringEntity, healLife = true, healMana = true)
                    }
                }
            }
        }
    }

    private fun World.forestEntranceTrigger(name: String): TriggerScript? {
        val player = this.family { all(Player) }.single()
        val quest = player[QuestLog].getOrNull<FlowerGirlQuest>()
        val hasTerealisFlower = hasItem(player, ItemType.TEREALIS_FLOWER)

        return when {
            quest == null || quest.isCompleted() || hasTerealisFlower -> trigger(name, this, player) {
                // player doesn't have the flower quest or already has the item or the quest is completed
                // -> remove the flower quest trigger of the map
                actionRemove(getEntityByTiledId(32))
            }

            else -> null
        }
    }

    private fun World.terealisFlowerTrigger(
        name: String,
        scriptEntity: Entity,
        triggeringEntity: Entity
    ): TriggerScript = trigger(name, this, triggeringEntity) {
        actionRemove(scriptEntity)
        actionAddItem(triggeringEntity, ItemType.TEREALIS_FLOWER)
    }

    companion object {
        private val log = logger<TriggerConfigurator>()
    }
}

