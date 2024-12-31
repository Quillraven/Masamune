package io.github.masamune.combat.effect

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Stats

data class HealEffect(
    override val source: Entity,
    override val target: Entity,
    val amountLife: Float,
    val amountMana: Float,
) : Effect {
    var targetLife = 0f
    var targetLifeMax = 0f
    var targetMana = 0f
    var targetManaMax = 0f

    override fun World.onStart() {
        val targetStats = target[Stats]
        targetStats.life = (targetStats.life + amountLife).coerceIn(0f, targetStats.totalLifeMax)
        targetStats.mana = (targetStats.mana + amountMana).coerceIn(0f, targetStats.totalManaMax)
        targetLife = targetStats.life
        targetLifeMax = targetStats.totalLifeMax
        targetMana = targetStats.mana
        targetManaMax = targetStats.totalManaMax
    }
}
