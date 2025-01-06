package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Experience(
    var level: Int = 1,
    var current: Int = 0,
    var forLevelUp: Int = 0,
) : Component<Experience> {

    init {
        forLevelUp = calcLevelUpXp(level)
    }

    override fun type() = Experience

    fun gainXp(xpGained: Int) {
        current += xpGained
        while (current >= forLevelUp) {
            forLevelUp = calcLevelUpXp(++level)
        }
    }

    companion object : ComponentType<Experience>() {
        fun calcLevelUpXp(forLevel: Int): Int = (100f + ((forLevel - 1) * 150) * 1.25f).toInt()

        fun calcLevelUps(level: Int, currentXp: Int, xpGain: Int): Int {
            var tmpLvl = level
            val tmpXp = currentXp + xpGain
            var tmpNeededXp = calcLevelUpXp(level)
            var levelUps = 0
            while (tmpXp >= tmpNeededXp) {
                ++levelUps
                tmpNeededXp = calcLevelUpXp(++tmpLvl)
            }
            return levelUps
        }
    }
}
