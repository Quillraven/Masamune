package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.action.Action
import io.github.masamune.combat.action.DefaultAction
import io.github.masamune.tiledmap.ActionType

data class Combat(
    val availableActions: List<ActionType>,
    var action: Action = DefaultAction,
    val targets: MutableEntityBag = MutableEntityBag(4),
    var attackSnd: SoundAsset = SoundAsset.ATTACK_SWIPE,
) : Component<Combat> {

    override fun type() = Combat

    fun clearAction() {
        action = DefaultAction
    }

    fun attackAction(): Action = availableActions.single { it == ActionType.ATTACK_SINGLE }()

    companion object : ComponentType<Combat>()
}
