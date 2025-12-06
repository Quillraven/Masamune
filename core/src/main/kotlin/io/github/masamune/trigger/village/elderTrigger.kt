package io.github.masamune.trigger.village

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.QuestLog
import io.github.masamune.quest.MainQuest
import io.github.masamune.tiledmap.ItemType
import io.github.masamune.trigger.TriggerScript
import io.github.masamune.trigger.trigger

fun World.elderTrigger(name: String, triggeringEntity: Entity): TriggerScript {
    val mainQuest = triggeringEntity.getOrNull(QuestLog)?.getOrNull<MainQuest>()

    return when {
        mainQuest != null && mainQuest.progress < 50 -> {
            trigger(name, this, triggeringEntity) {
                actionDialog("elder_10")
            }
        }

        mainQuest != null && mainQuest.progress == 50 -> {
            trigger(name, this, triggeringEntity) {
                actionDialog("elder_20")
                actionCompleteQuest(mainQuest)
                actionDialog("demo_end")
            }
        }

        mainQuest != null && mainQuest.isCompleted() -> {
            trigger(name, this, triggeringEntity) {
                actionDialog("elder_30")
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
