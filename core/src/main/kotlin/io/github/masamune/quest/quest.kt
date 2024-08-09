package io.github.masamune.quest

import io.github.masamune.event.Event
import io.github.masamune.event.EventListener

sealed interface Quest : EventListener {
    fun isCompleted(): Boolean

    override fun onEvent(event: Event) = Unit
}

data class MainQuest(var progress: Int = 0) : Quest {
    override fun isCompleted(): Boolean = progress == 100
}

data class FlowerGirlQuest(var progress: Int = 0) : Quest {
    override fun isCompleted(): Boolean = progress == 100
}
