package io.github.masamune.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.dialog.Dialog

sealed interface Event

data class MapChangeEvent(val tiledMap: TiledMap) : Event

data class PlayerMoveEvent(val direction: Vector2) : Event

data class PlayerInteractBeginContactEvent(val player: Entity, val other: Entity) : Event

data class PlayerInteractEndContactEvent(val player: Entity, val other: Entity) : Event

data object PlayerInteractEvent : Event

data class DialogBeginEvent(val world: World, val player: Entity, val other: Entity, val dialog: Dialog) : Event

data class DialogEndEvent(val player: Entity, val other: Entity, val dialog: Dialog, val optionIdx: Int) : Event

data object UiUpEvent : Event

data object UiDownEvent : Event

data object UiSelectEvent : Event
