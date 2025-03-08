package io.github.masamune.trigger.forest.entrance

import com.github.quillraven.fleks.World
import io.github.masamune.component.Player
import io.github.masamune.component.QuestLog
import io.github.masamune.getEntityByTiledId
import io.github.masamune.hasItem
import io.github.masamune.quest.FlowerGirlQuest
import io.github.masamune.quest.MainQuest
import io.github.masamune.screen.CutSceneScreen
import io.github.masamune.screen.FadeTransitionType
import io.github.masamune.tiledmap.ItemType
import io.github.masamune.trigger.TriggerScript
import io.github.masamune.trigger.trigger

fun World.forestEntranceTrigger(name: String): TriggerScript? {
    val player = this.family { all(Player) }.single()

    // check main quest progress: if boss fight is completed then demo is over
    val mainQuest = player[QuestLog].get<MainQuest>()
    if (mainQuest.progress >= 50) {
        return trigger(name, this, player) {
            actionChangeScreen {
                transitionScreen<CutSceneScreen>(
                    FadeTransitionType(1f, 0f, 3f),
                    FadeTransitionType(0f, 1f, 1f)
                ) {
                    it.startCutScene("outro")
                }
            }
        }
    }

    val flowerQuest = player[QuestLog].getOrNull<FlowerGirlQuest>()
    val hasTerealisFlower = hasItem(player, ItemType.TEREALIS_FLOWER)
    return when {
        flowerQuest == null || flowerQuest.isCompleted() || hasTerealisFlower -> trigger(name, this, player) {
            // player doesn't have the flower quest or already has the item or the quest is completed
            // -> remove the flower quest trigger of the map
            actionRemove(getEntityByTiledId(32))
        }

        else -> null
    }
}
