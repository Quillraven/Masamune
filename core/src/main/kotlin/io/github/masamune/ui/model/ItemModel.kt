package io.github.masamune.ui.model

import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import io.github.masamune.tiledmap.ItemCategory

data class ItemModel(
    val name: String,
    val cost: Int,
    val description: String,
    val category: ItemCategory,
    val image: Drawable?
)
