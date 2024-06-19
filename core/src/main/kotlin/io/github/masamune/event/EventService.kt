package io.github.masamune.event

import com.github.quillraven.fleks.World
import ktx.log.logger

interface EventListener {
    fun onEvent(event: Event)
}

class EventService {

    private val listeners = mutableListOf<EventListener>()
    private val eventQueue = ArrayDeque<Event>(4)

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
        eventQueue.addLast(event)
        if (eventQueue.size > 1) {
            return
        }

        while (eventQueue.isNotEmpty()) {
            val eventToFire = eventQueue.first()
            log.debug { "Firing event $eventToFire" }
            listeners.forEach { it.onEvent(eventToFire) }
            eventQueue.removeFirst()
        }
    }

    companion object {
        private val log = logger<EventService>()
    }
}
