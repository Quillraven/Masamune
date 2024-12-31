package io.github.masamune.combat.buff

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.collection.MutableEntityBag
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.ActionExecutorService

data class ReflectMagicBuff(
    override val owner: Entity,
    private var numMagic: Int,
    private val reflectPercentage: Float,
    private val alreadyReflected: MutableEntityBag = MutableEntityBag(4),
) : OnMagicDamageTakenBuff {
    override fun ActionExecutorService.postMagicDamageTaken(source: Entity, target: Entity, damage: Float) {
        if (source in alreadyReflected) {
            return
        }

        alreadyReflected += source
        --numMagic
        val reflectDamage = damage * reflectPercentage
        dealMagicDamage(owner, reflectDamage, source, "fire1", 0.5f, 1.5f, SoundAsset.EXPLOSION1, 1f)
        if (numMagic <= 0f) {
            removeBuff()
        }
    }

    override fun onActionEnd() {
        alreadyReflected.clear()
    }
}
