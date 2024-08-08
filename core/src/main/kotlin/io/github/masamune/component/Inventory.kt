package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag

data class Inventory(
    val items: MutableEntityBag = MutableEntityBag(16),
) : Component<Inventory> {
    override fun type() = Inventory

    companion object : ComponentType<Inventory>()
}
