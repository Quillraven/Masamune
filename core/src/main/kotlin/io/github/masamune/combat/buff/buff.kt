package io.github.masamune.combat.buff

import com.github.quillraven.fleks.Entity
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.ActionExecutorService

sealed interface Buff {
    val owner: Entity
}

sealed interface OnUpdateBuff {
    fun ActionExecutorService.onStart() = Unit
    fun ActionExecutorService.onUpdate(): Boolean = true
    fun ActionExecutorService.onFinish() = Unit
}

sealed interface OnAttackDamageBuff : Buff {
    fun ActionExecutorService.preAttackDamage(source: Entity, target: Entity, damage: Float): Float = damage
    fun ActionExecutorService.postAttackDamage(source: Entity, target: Entity, damage: Float) = Unit
}

sealed interface OnDamageTakenBuff : Buff {
    fun ActionExecutorService.preDamageTaken(source: Entity, target: Entity, damage: Float): Float = damage
    fun ActionExecutorService.postDamageTaken(source: Entity, target: Entity, damage: Float) = Unit
}

class NullifyAttackBuff(override val owner: Entity, private var numAttacks: Int) : OnDamageTakenBuff {

    override fun ActionExecutorService.preDamageTaken(
        source: Entity,
        target: Entity,
        damage: Float
    ): Float {
        --numAttacks
        addSfx(owner, "shield-yellow", 0.5f, 1.5f)
        if (numAttacks <= 0) {
            removeBuff()
        }
        return 0f
    }
}

class ReflectAttackBuff(
    override val owner: Entity,
    private var numAttacks: Int,
    private val reflectPercentage: Float,
) : OnDamageTakenBuff, OnUpdateBuff {
    private var reflectDamage = 0f
    private var reflectTarget: Entity = Entity.NONE

    override fun ActionExecutorService.postDamageTaken(source: Entity, target: Entity, damage: Float) {
        --numAttacks
        reflectDamage = damage * reflectPercentage
        reflectTarget = source
        if (numAttacks <= 0f) {
            removeBuff()
        }
    }

    override fun ActionExecutorService.onUpdate(): Boolean {
        dealMagicDamage(reflectDamage, reflectTarget, "shield-yellow", 0.5f, 1.5f, SoundAsset.SWORD_SWIPE, 1f)
        return true
    }
}
