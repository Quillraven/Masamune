package io.github.masamune.combat.action

import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.ActionExecutorService
import io.github.masamune.tiledmap.ActionType

class ItemManaRestoreAction : Action(ActionType.ITEM_MANA_RESTORE, ActionTargetType.SINGLE, defensive = true) {

    override fun ActionExecutorService.onUpdate(): Boolean {
        heal(life = 0f, mana = source.stats.mana, target = singleTarget)
        play(SoundAsset.HEAL1, 1.5f)
        return true
    }

}
