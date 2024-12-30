package io.github.masamune.combat.buff

import com.github.quillraven.fleks.Entity
import io.github.masamune.combat.ActionExecutorService

sealed interface Buff {
    val owner: Entity
}

sealed interface BuffOnAttackDamage : Buff {
    fun ActionExecutorService.preAttackDamage(source: Entity, target: Entity, damage: Float): Float = damage
    fun ActionExecutorService.postAttackDamage(source: Entity, target: Entity, damage: Float) = Unit
}

class NullifyBuff(override val owner: Entity, private var numTurns: Int) : BuffOnAttackDamage {

    override fun ActionExecutorService.preAttackDamage(
        source: Entity,
        target: Entity,
        damage: Float
    ): Float {
        if (owner == target) {
            --numTurns
            addSfx(owner, "shield-yellow", 0.5f, 1.5f)
            if (numTurns <= 0) {
                removeBuff()
            }
            return 0f
        }
        return damage
    }
}
