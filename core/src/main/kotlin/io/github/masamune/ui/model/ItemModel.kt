package io.github.masamune.ui.model

import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.tiledmap.ItemType
import io.github.masamune.tiledmap.TiledStats

data class ItemModel(
    val type: ItemType,
    val stats: TiledStats,
    val name: String,
    val cost: Int,
    val description: String,
    val category: ItemCategory,
    val image: Drawable?,
    var selected: Int = 0, // amount to buy/sell
    var amount: Int = 0, // current player amount
)
