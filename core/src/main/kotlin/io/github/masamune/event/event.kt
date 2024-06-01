package io.github.masamune.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity

sealed interface Event

data class MapChangeEvent(val tiledMap: TiledMap) : Event

data class PlayerMoveEvent(val direction: Vector2) : Event

data class PlayerInteractBeginContactEvent(val player: Entity, val other: Entity) : Event

data class PlayerInteractEndContactEvent(val player: Entity, val other: Entity) : Event

data object PlayerInteractEvent : Event
