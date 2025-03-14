package io.github.masamune.combat.buff

import com.github.quillraven.fleks.Entity
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.ActionExecutorService

class PoisonBuff(
    override val owner: Entity,
    private val damageFactor: Float,
    private var duration: Int,
) : OnTurnBuff {

    override fun ActionExecutorService.onTurnEnd() {
        val damage = owner.stats.lifeMax * damageFactor
        dealDoTDamage(damage, owner, "slow1", 1.25f, 1.75f, SoundAsset.SLOW, 1f)
        if (--duration <= 0) {
            removeBuff()
        }
    }

}
