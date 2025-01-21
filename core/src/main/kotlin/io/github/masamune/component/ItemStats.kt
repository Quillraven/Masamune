package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ItemStats(
    val agility: Float = 0f,
    val arcaneStrike: Float = 0f,
    val armor: Float = 0f,
    val constitution: Float = 0f,
    val criticalStrike: Float = 0f,
    val damage: Float = 0f,
    val intelligence: Float = 0f,
    val life: Float = 0f,
    val lifeMax: Float = 0f,
    val magicalEvade: Float = 0f,
    val mana: Float = 0f,
    val manaMax: Float = 0f,
    val physicalEvade: Float = 0f,
    val resistance: Float = 0f,
    val strength: Float = 0f,
) : Component<ItemStats> {
    override fun type() = ItemStats

    companion object : ComponentType<ItemStats>()
}
