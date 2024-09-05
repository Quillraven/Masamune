package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import kotlin.math.floor

data class Experience(
    var level: Int = 1,
    var current: Int = 0,
    var forLevelUp: Int = 0,
) : Component<Experience> {

    init {
        calcLevelUpXp()
    }

    fun calcLevelUpXp() {
        val xpNeeded: Float = 250f + ((level - 1) * 200) * 1.25f
        forLevelUp = (floor(xpNeeded / 10) * 10).toInt()
    }

    override fun type() = Experience

    companion object : ComponentType<Experience>()
}
