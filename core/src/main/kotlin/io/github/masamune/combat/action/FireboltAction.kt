package io.github.masamune.combat.action

import com.github.quillraven.fleks.Entity
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.ActionExecutorService

class FireboltAction : Action(ActionTargetType.MULTI) {
    override fun ActionExecutorService.canPerform(entity: Entity) = hasMana(3)

    override fun ActionExecutorService.onUpdate(): Boolean {
        dealDamage(physical = 0f, magical = 6f / numTargets, targets = allTargets)
        play(SoundAsset.EXPLOSION1, 1f)
        return true
    }
}
