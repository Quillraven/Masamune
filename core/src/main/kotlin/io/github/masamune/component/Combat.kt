package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.action.Action
import io.github.masamune.combat.action.DefaultAction
import io.github.masamune.tiledmap.ActionType

data class Combat(
    val availableActionTypes: List<ActionType>,
    var action: Action = DefaultAction,
    val targets: MutableEntityBag = MutableEntityBag(4),
    var attackSnd: SoundAsset = SoundAsset.ATTACK_SWIPE,
) : Component<Combat> {

    val attackAction: Action by lazy { availableActionTypes.single { it == ActionType.ATTACK_SINGLE }() }

    val magicActions: List<Action> by lazy {
        availableActionTypes
            // attack actions should not show up as "magic" actions in combat
            // use item action should also not show up us "magic"
            .filter { it != ActionType.ATTACK_SINGLE && it != ActionType.USE_ITEM }
            .map { it() }
    }

    override fun type() = Combat

    fun clearAction() {
        action = DefaultAction
    }

    companion object : ComponentType<Combat>()
}
