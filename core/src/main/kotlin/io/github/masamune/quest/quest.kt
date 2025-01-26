package io.github.masamune.quest

import io.github.masamune.event.Event
import io.github.masamune.event.EventListener

sealed interface Quest : EventListener {
    val i18nKey: String

    fun isCompleted(): Boolean

    override fun onEvent(event: Event) = Unit
}

data class MainQuest(var progress: Int = 0) : Quest {
    override val i18nKey = "quest.main"

    override fun isCompleted(): Boolean = progress == 100
}

data class FlowerGirlQuest(var progress: Int = 0) : Quest {
    override val i18nKey = "quest.flower_girl"

    override fun isCompleted(): Boolean = progress == 100
}
