package io.github.masamune.tiledmap

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapProperties
import com.badlogic.gdx.maps.tiled.TiledMapTile
import ktx.tiled.property

import ktx.tiled.propertyOrNull

// This is an autogenerated file by gradle's 'genTiledEnumsAndExtensions' task. Do not touch it!

val MapObject.density: Float
    get() = this.property<Float>("density", 0f)

val MapObject.friction: Float
    get() = this.property<Float>("friction", 0f)

val MapObject.isSensor: Boolean
    get() = this.property<Boolean>("isSensor", false)

val MapObject.restitution: Float
    get() = this.property<Float>("restitution", 0f)

val MapObject.userData: String?
    get() = this.propertyOrNull<String>("userData")

val MapObject.targetPortalId: Int
    get() = this.property<Int>("targetPortalId", 0)

val TiledMapTile.action: String
    get() = this.property<String>("action", "")

val TiledMapTile.atlas: String
    get() = this.property<String>("atlas", "UNDEFINED")

val TiledMapTile.category: String
    get() = this.property<String>("category", "OTHER")

val TiledMapTile.consumableType: String
    get() = this.property<String>("consumableType", "UNDEFINED")

val TiledMapTile.cost: Int
    get() = this.property<Int>("cost", 0)

val TiledMapTile.itemType: String
    get() = this.property<String>("itemType", "UNDEFINED")

val TiledMapTile.speed: Float
    get() = this.property<Float>("speed", 0f)

val TiledMapTile.stats: MapProperties?
    get() = this.propertyOrNull<MapProperties>("stats")

val TiledMapTile.behavior: String
    get() = this.property<String>("behavior", "default")

val TiledMapTile.combatActions: String
    get() = this.property<String>("combatActions", "")

val TiledMapTile.level: Int
    get() = this.property<Int>("level", 0)

val TiledMapTile.objType: String
    get() = this.property<String>("objType", "UNDEFINED")

val TiledMapTile.talons: Int
    get() = this.property<Int>("talons", 0)

val TiledMapTile.xp: Int
    get() = this.property<Int>("xp", 0)

val TiledMapTile.triggerName: String
    get() = this.property<String>("triggerName", "")
