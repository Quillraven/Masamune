package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.tiledmap.ItemType

data class Item(
    val type: ItemType,
    val cost: Int,
    val category: ItemCategory,
    val descriptionKey: String, // key in i18n bundle
) : Component<Item> {
    override fun type() = Item

    companion object : ComponentType<Item>()
}
