package io.github.masamune.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.github.quillraven.fleks.World

sealed interface Event

data class MapChangeEvent(val tiledMap: TiledMap) : Event

interface EventListener {
    fun onEvent(event: Event)
}

class EventService {
    private val listeners = mutableListOf<EventListener>()

    operator fun plusAssign(listener: EventListener) {
        listeners += listener
    }

    fun registerSystems(world: World) {
        world.systems
            .filterIsInstance<EventListener>()
            .forEach { this += it }
    }

    fun unregisterSystems(world: World) {
        world.systems
            .filterIsInstance<EventListener>()
            .forEach { this -= it }
    }

    operator fun minusAssign(listener: EventListener) {
        listeners -= listener
    }

    fun fire(event: Event) {
        listeners.forEach { it.onEvent(event) }
    }
}
