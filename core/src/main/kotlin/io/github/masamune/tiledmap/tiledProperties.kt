package io.github.masamune.tiledmap

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMapTile
import ktx.tiled.property

import ktx.tiled.propertyOrNull

// This is an autogenerated file by gradle's 'genTiledEnumsAndExtensions' task. Do not touch it!

val TiledMapTile.atlas: String
    get() = this.property<String>("atlas", "UNDEFINED")

val TiledMapTile.atlasRegionKey: String
    get() = this.property<String>("atlasRegionKey", "")

val TiledMapTile.bodyType: String
    get() = this.property<String>("bodyType", "UNDEFINED")

val TiledMapTile.color: String
    get() = this.property<String>("color", "#ffffffff")

val TiledMapTile.dialogName: String
    get() = this.property<String>("dialogName", "")

val TiledMapTile.hasAnimation: Boolean
    get() = this.property<Boolean>("hasAnimation", false)

val TiledMapTile.objType: String
    get() = this.property<String>("objType", "UNDEFINED")

val TiledMapTile.speed: Float
    get() = this.property<Float>("speed", 0f)

val TiledMapTile.triggerName: String
    get() = this.property<String>("triggerName", "")

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

val TiledMapTile.itemName: String
    get() = this.property<String>("itemName", "")

val TiledMapTile.stats: TiledStats
    get() = this.property<TiledStats>("stats")
