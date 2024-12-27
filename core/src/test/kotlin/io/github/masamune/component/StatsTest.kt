package io.github.masamune.component

import io.kotest.matchers.floats.shouldBeExactly
import kotlin.test.Test

class StatsTest {

    @Test
    fun `test total calculation (agility)`() {
        val stats = Stats()

        stats.agility = 10f
        stats.totalAgility shouldBeExactly 10f

        // percentage bonus of 100% (=value is doubled)
        stats.percModifier.agility = 1f
        stats.totalAgility shouldBeExactly 20f
    }

    @Test
    fun `test andEquipment (agility)`() {
        val equipment = listOf(
            Stats(agility = 5f).apply { percModifier.agility = 1f },
            Stats(agility = 5f).apply { percModifier.agility = 1f },
        )
        val stats = Stats(agility = 10f)

        stats andEquipment equipment
        stats.totalAgility shouldBeExactly 60f
    }

}
