package io.github.masamune.event

import com.badlogic.gdx.maps.tiled.TiledMap

sealed interface Event

data class MapChangeEvent(val tiledMap: TiledMap) : Event
