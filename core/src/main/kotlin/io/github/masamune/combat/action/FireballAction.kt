package io.github.masamune.combat.action

import com.github.quillraven.fleks.Entity
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.ActionExecutorService

class FireballAction : Action(ActionTargetType.ALL) {
    private var amount = 2

    override fun ActionExecutorService.canPerform(entity: Entity) = hasMana(5)

    override fun ActionExecutorService.onUpdate(): Boolean {
        amount--
        dealDamage(physical = 0f, magical = 10f, targets = allTargets)
        play(SoundAsset.EXPLOSION1, 1f)

        return amount <= 0
    }
}
