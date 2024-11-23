package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag

data class Equipment(
    val items: MutableEntityBag = MutableEntityBag(8),
) : Component<Equipment> {
    override fun type() = Equipment

    companion object : ComponentType<Equipment>()
}
