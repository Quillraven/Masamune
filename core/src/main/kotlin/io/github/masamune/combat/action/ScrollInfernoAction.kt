package io.github.masamune.combat.action

import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.ActionExecutorService
import io.github.masamune.tiledmap.ActionType

class ScrollInfernoAction : Action(ActionType.SCROLL_INFERNO, ActionTargetType.ALL) {

    override fun ActionExecutorService.onUpdate(): Boolean {
        dealDamage(physical = 0f, magical = source.stats.damage, targets = allTargets)
        play(SoundAsset.EXPLOSION1, 1f)
        return true
    }

}
