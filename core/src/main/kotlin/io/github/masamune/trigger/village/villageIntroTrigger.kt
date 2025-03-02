package io.github.masamune.trigger.village

import com.github.quillraven.fleks.World
import io.github.masamune.component.Move
import io.github.masamune.component.Player
import io.github.masamune.component.QuestLog
import io.github.masamune.quest.MainQuest
import io.github.masamune.trigger.TriggerScript
import io.github.masamune.trigger.trigger

fun World.villageIntroTrigger(name: String): TriggerScript? {
    val player = family { all(Player) }.single()
    val mainQuest = player[QuestLog].getOrNull<MainQuest>()
    val originalSpeed = player[Move].speed

    return when (mainQuest) {
        null -> trigger(name, this, player) {
            val selector = selectEntity { player }
            actionEnableInput(false)
            actionEntitySpeed(selector, 1.5f)
            actionFollowPath(selector, 21, removeAtEnd = true, waitForEnd = true)
            actionEntitySpeed(selector, originalSpeed)
            actionEnableInput(true)
            actionDialog("village_intro")
        }

        else -> null
    }
}
