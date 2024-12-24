package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.masamune.combat.action.Action
import io.github.masamune.tiledmap.ActionType
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.tiledmap.ItemType

data class Item(
    val type: ItemType,
    val cost: Int,
    val category: ItemCategory,
    val descriptionKey: String, // key in i18n bundle
    val actionType: ActionType,
    var amount: Int = 1,
    val onlyCombat: Boolean = false,
) : Component<Item> {
    val action: Action by lazy { actionType() }

    override fun type() = Item

    companion object : ComponentType<Item>()
}
