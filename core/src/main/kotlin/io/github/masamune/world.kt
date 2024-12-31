package io.github.masamune

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.CachingAtlas
import io.github.masamune.component.Animation
import io.github.masamune.component.Graphic
import io.github.masamune.component.Inventory
import io.github.masamune.component.Item
import io.github.masamune.component.Selector
import io.github.masamune.component.Stats
import io.github.masamune.component.Transform
import io.github.masamune.tiledmap.AnimationType
import io.github.masamune.tiledmap.ItemType
import ktx.app.gdxError
import ktx.log.Logger
import ktx.math.vec2
import ktx.math.vec3

private val log = Logger("World")

const val SELECTOR_SPEED = 1.5f
const val SELECTOR_SCALE = 1.2f

fun World.selectorEntity(target: Entity, confirmed: Boolean) = this.entity {
    // position and size don't matter because they get updated in the SelectorSystem
    val atlas = inject<CachingAtlas>(AtlasAsset.CHARS_AND_PROPS.name)
    it += Transform(vec3(), vec2(), SELECTOR_SCALE)
    val animationCmp = Animation.ofAtlas(atlas, "select", AnimationType.IDLE, speed = SELECTOR_SPEED)
    it += animationCmp
    it += Graphic(animationCmp.gdxAnimation.getKeyFrame(0f))
    it += Selector(target, confirmed)
}

fun World.addItem(itemEntity: Entity, to: Entity) {
    val itemCmp = itemEntity[Item]
    val items = to[Inventory].items
    val existingItem = items.firstOrNull { it[Item].type == itemCmp.type }
    if (existingItem == null) {
        // item not yet in inventory -> add it
        log.debug { "Adding new item of type ${itemCmp.type} to inventory" }
        items += itemEntity
        return
    }

    // item already in inventory -> increase amount
    log.debug { "Increasing amount of item ${itemCmp.type} by ${itemCmp.amount}" }
    existingItem[Item].amount += itemCmp.amount
}

fun World.removeItem(type: ItemType, amount: Int, from: Entity) {
    val items = from[Inventory].items
    val existingItem = items.firstOrNull { it[Item].type == type }
    if (existingItem == null) {
        gdxError("Cannot remove a non-existing item of type $type")
    }

    log.debug { "Decreasing amount of item $type by $amount" }
    existingItem[Item].amount -= amount
    if (existingItem[Item].amount <= 0) {
        log.debug { "Removing item of type $type from inventory" }
        items -= existingItem
        existingItem.remove()
    }
}

fun World.removeItem(item: Entity, from: Entity) {
    val items = from[Inventory].items
    items -= item
    item.remove()
}

fun World.isEntityDead(entity: Entity): Boolean = with(this) {
    return entity[Stats].life < 1f
}

fun World.isEntityAlive(entity: Entity): Boolean = with(this) {
    return entity[Stats].life >= 1f
}
