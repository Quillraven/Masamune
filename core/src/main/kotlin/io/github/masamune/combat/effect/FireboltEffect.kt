package io.github.masamune.combat.effect

import com.github.quillraven.fleks.Entity
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.Action
import io.github.masamune.combat.ActionTargetType

class FireboltEffect : Effect(ActionTargetType.MULTI) {
    override fun Action.canPerform(entity: Entity) = hasMana(3)

    override fun Action.onUpdate(): Boolean {
        dealDamage(physical = 0f, magical = 6f / numTargets, targets = allTargets)
        play(SoundAsset.EXPLOSION1, 1f)
        return true
    }
}
