package io.github.masamune.combat.effect

import com.github.quillraven.fleks.Entity
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.Action
import io.github.masamune.combat.ActionTargetType

class FireballEffect : Effect(ActionTargetType.ALL) {
    private var amount = 2

    override fun Action.canPerform(entity: Entity) = hasMana(5)

    override fun Action.onUpdate(): Boolean {
        amount--
        dealDamage(physical = 0f, magical = 10f, targets = allTargets)
        play(SoundAsset.EXPLOSION1, 1f)

        return amount <= 0
    }
}
