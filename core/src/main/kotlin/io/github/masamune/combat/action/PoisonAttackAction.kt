package io.github.masamune.combat.action

import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.ActionExecutorService
import io.github.masamune.combat.buff.PoisonBuff
import io.github.masamune.tiledmap.ActionType

class PoisonAttackAction : Action(ActionType.POISON_ATTACK, ActionTargetType.SINGLE) {
    override fun ActionExecutorService.onStart() {
        addBuff(PoisonBuff(singleTarget, 2f, 2))
        play(SoundAsset.SLOW)
        addSfx(singleTarget, "slow1", 1.25f, 1.75f)
        wait(1.5f)
    }
}
