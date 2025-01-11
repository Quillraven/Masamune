package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.MutableEntityBag
import io.github.masamune.tiledmap.ActionType

data class Equipment(
    val items: MutableEntityBag = MutableEntityBag(8),
) : Component<Equipment> {
    override fun type() = Equipment

    fun World.toStats(): Stats {
        val result = Stats()
        items.forEach {
            result += it[Stats]
        }
        return result
    }

    fun World.toActionTypes(): List<ActionType> = items.map { it[Item].actionType }

    companion object : ComponentType<Equipment>()
}
