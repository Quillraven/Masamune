package io.github.masamune.event

import com.github.quillraven.fleks.World

interface EventListener {
    fun onEvent(event: Event)
}

class EventService {
    private val listeners = mutableListOf<EventListener>()

    val numListeners: Int
        get() = listeners.size

    operator fun plusAssign(listener: EventListener) {
        if (listener in listeners) {
            return
        }

        listeners += listener
    }

    operator fun plusAssign(world: World) {
        world.systems
            .filterIsInstance<EventListener>()
            .forEach { this += it }
    }

    operator fun minusAssign(world: World) {
        world.systems
            .filterIsInstance<EventListener>()
            .forEach { this -= it }
    }

    operator fun minusAssign(listener: EventListener) {
        listeners -= listener
    }

    operator fun contains(listener: EventListener): Boolean = listener in listeners

    fun fire(event: Event) {
        listeners.forEach { it.onEvent(event) }
    }
}
