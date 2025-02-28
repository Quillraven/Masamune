package io.github.masamune.trigger.forest.entrance

import com.github.quillraven.fleks.World
import io.github.masamune.component.Player
import io.github.masamune.component.QuestLog
import io.github.masamune.getEntityByTiledId
import io.github.masamune.hasItem
import io.github.masamune.quest.FlowerGirlQuest
import io.github.masamune.tiledmap.ItemType
import io.github.masamune.trigger.TriggerScript
import io.github.masamune.trigger.trigger

fun World.forestEntranceTrigger(name: String): TriggerScript? {
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
