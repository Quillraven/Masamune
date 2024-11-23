package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.MutableEntityBag
import ktx.log.logger

data class Inventory(
    val items: MutableEntityBag = MutableEntityBag(16),
    var talons: Int = 0, // = money
) : Component<Inventory> {
    override fun type() = Inventory

    companion object : ComponentType<Inventory>() {
        private val log = logger<Inventory>()

        fun World.addItem(itemEntity: Entity, to: Entity) {
            val (type, _, _, _, amount) = itemEntity[Item]
            val items = to[Inventory].items
            val existingItem = items.firstOrNull { it[Item].type == type }
            if (existingItem == null) {
                // item not yet in inventory -> add it
                log.debug { "Adding new item of type $type to inventory" }
                items += itemEntity
                return
            }

            // item already in inventory -> increase amount
            log.debug { "Increasing amount of item $type by $amount" }
            existingItem[Item].amount += amount
        }
    }
}
