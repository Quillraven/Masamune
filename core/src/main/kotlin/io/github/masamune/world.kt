package io.github.masamune

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.CachingAtlas
import io.github.masamune.combat.ActionExecutorService
import io.github.masamune.combat.ActionExecutorService.Companion.LIFE_PER_CONST
import io.github.masamune.combat.action.Action
import io.github.masamune.component.Animation
import io.github.masamune.component.Equipment
import io.github.masamune.component.Graphic
import io.github.masamune.component.Inventory
import io.github.masamune.component.Item
import io.github.masamune.component.Player
import io.github.masamune.component.QuestLog
import io.github.masamune.component.Remove
import io.github.masamune.component.Selector
import io.github.masamune.component.Stats
import io.github.masamune.component.Transform
import io.github.masamune.quest.Quest
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

fun World.addItem(
    itemEntity: Entity,
    to: Entity,
    equipItem: Boolean = true,
) {
    val itemCmp = itemEntity[Item]
    val items = to[Inventory].items
    val existingItem = items.firstOrNull { it[Item].type == itemCmp.type }
    if (existingItem != null) {
        // item already in inventory -> increase amount
        log.debug { "Increasing amount of item ${itemCmp.type} by ${itemCmp.amount}" }
        existingItem[Item].amount += itemCmp.amount
        return
    }

    // if item is an equipment and 'to' entity is a player then equip the item instead of
    // adding it to the inventory, if no item of that type is equipped yet
    if (equipItem && itemCmp.category.isEquipment && to has Player) {
        val equipmentItems = to[Equipment].items
        if (equipmentItems.none { it[Item].category == itemCmp.category }) {
            equipItem(itemEntity, to)
            return
        }
    }

    // item not yet in inventory -> add it
    log.debug { "Adding new item of type ${itemCmp.type} to inventory" }
    items += itemEntity
}

fun World.equipItem(itemEntity: Entity, to: Entity) {
    val itemCmp = itemEntity[Item]
    val equipmentItems = to[Equipment].items
    log.debug { "Equipping item ${itemCmp.type}" }

    // remove currently equipped item, if there is any
    equipmentItems.singleOrNull { it[Item].category == itemCmp.category }?.let { existingItem ->
        equipmentItems -= existingItem
        // move item to inventory
        addItem(existingItem, to, equipItem = false)
    }

    // equip item
    equipmentItems += itemEntity

    // adjust life/mana if necessary
    val itemStats = itemEntity[Stats]
    if (itemStats.lifeMax != 0f || itemStats.constitution != 0f) {
        // adjust life in case of lifeMax or constitution bonus. Life percentage should remain the same.
        val playerStats = to[Stats]
        val lifePerc = playerStats.life / playerStats.totalLifeMax
        val newLifeMax = playerStats.totalLifeMax + itemStats.lifeMax + (itemStats.constitution * LIFE_PER_CONST)
        playerStats.life = newLifeMax * lifePerc
    }

    if (itemStats.manaMax != 0f) {
        // adjust mana in case of manaMax bonus. Mana percentage should remain the same.
        val playerStats = to[Stats]
        val manaPerc = playerStats.mana / playerStats.totalManaMax
        val newManaMax = playerStats.totalManaMax + itemStats.manaMax
        playerStats.mana = newManaMax * manaPerc
    }
}

fun World.removeItem(type: ItemType, amount: Int, from: Entity, removeEntity: Boolean = true): Boolean {
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
        if (removeEntity) {
            existingItem.remove()
        }
        return true
    }

    return false
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

fun World.canPerformAction(entity: Entity, action: Action): Boolean {
    val actionExecutorService = inject<ActionExecutorService>()
    return action.run { actionExecutorService.canPerform(entity) }
}

inline fun <reified T : Quest> World.hasQuest(entity: Entity): Boolean {
    return entity[QuestLog].getOrNull<T>() != null
}

fun World.spawnSfx(target: Entity, sfxAtlasKey: String, duration: Float, scale: Float = 1f) {
    val (toPos, toSize, toScale) = target[Transform]
    val sfxAtlas = inject<CachingAtlas>(AtlasAsset.SFX.name)

    entity {
        it += Transform(toPos.cpy().apply { z = 3f }, toSize.cpy(), toScale * scale)
        val animation = Animation.ofAtlas(sfxAtlas, sfxAtlasKey, AnimationType.IDLE)
        animation.speed = 1f / (duration / animation.gdxAnimation.animationDuration)
        animation.playMode = com.badlogic.gdx.graphics.g2d.Animation.PlayMode.NORMAL
        it += animation
        it += Graphic(animation.gdxAnimation.getKeyFrame(0f))
        it += Remove(duration)
    }
}
