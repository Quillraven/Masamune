package io.github.masamune.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2

sealed interface Event

data class MapChangeEvent(val tiledMap: TiledMap) : Event

data class PlayerMoveEvent(val direction: Vector2) : Event
