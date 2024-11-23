package io.github.masamune.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.dialog.Dialog
import io.github.masamune.tiledmap.MapTransitionType
import io.github.masamune.ui.model.MenuType

sealed interface Event

data class MapChangeEvent(val tiledMap: TiledMap) : Event

data class PlayerMoveEvent(val direction: Vector2) : Event

data class PlayerInteractBeginContactEvent(val player: Entity, val other: Entity) : Event

data class PlayerInteractEndContactEvent(val player: Entity, val other: Entity) : Event

data object PlayerInteractEvent : Event

data class MenuBeginEvent(val type: MenuType) : Event

data object MenuEndEvent : Event

data object GameExitEvent : Event

data class DialogBeginEvent(val world: World, val player: Entity, val dialog: Dialog) : Event

data class DialogEndEvent(val player: Entity, val dialog: Dialog, val optionIdx: Int) : Event

data object DialogOptionTriggerEvent : Event

data object DialogOptionChangeEvent : Event

data object DialogBackEvent : Event

data object UiUpEvent : Event

data object UiDownEvent : Event

data object UiRightEvent : Event

data object UiLeftEvent : Event

data object UiSelectEvent : Event

data object UiBackEvent : Event

data class ShopBeginEvent(val world: World, val player: Entity, val shop: Entity) : Event

data class MapTransitionBeginEvent(
    val fromTiledMap: TiledMap,
    val toTiledMap: TiledMap,
    val time: Float,
    val interpolation: Interpolation,
    val type: MapTransitionType,
    val mapOffset: Vector2,
    val newPlayerPos: Vector2, // position in new map (=toTiledMap)
) : Event

data object MapTransitionEndEvent : Event
