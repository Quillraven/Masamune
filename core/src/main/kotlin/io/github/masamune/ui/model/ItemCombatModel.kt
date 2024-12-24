package io.github.masamune.ui.model

import io.github.masamune.tiledmap.ItemType

data class ItemCombatModel(
    val type: ItemType,
    val name: String,
    val targetDescriptor: String,
    val amount: Int,
)
