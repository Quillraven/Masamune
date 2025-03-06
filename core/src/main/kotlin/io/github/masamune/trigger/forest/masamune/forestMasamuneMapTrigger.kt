package io.github.masamune.trigger.forest.masamune

import com.github.quillraven.fleks.World
import io.github.masamune.component.Player
import io.github.masamune.component.QuestLog
import io.github.masamune.component.Trigger
import io.github.masamune.getEntityByTiledId
import io.github.masamune.quest.MainQuest
import io.github.masamune.trigger.TriggerScript
import io.github.masamune.trigger.trigger

fun World.forestMasamuneMapTrigger(name: String): TriggerScript? {
    val player = this.family { all(Player) }.single()
    val quest = player[QuestLog].get<MainQuest>()
    val masamuneAlreadyTriggered = quest.progress >= 50

    return when {
        masamuneAlreadyTriggered -> trigger(name, this, player) {
            actionConfigureEntity(getEntityByTiledId(34)) {
                it -= Trigger
            }
        }

        else -> null
    }
}
