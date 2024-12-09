package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.masamune.combat.Action
import io.github.masamune.combat.DefaultAction

data class Combat(
    var action: Action = DefaultAction,
) : Component<Combat> {
    val hasAction: Boolean
        get() = action != DefaultAction

    override fun type() = Combat

    fun clearAction() {
        action = DefaultAction
    }

    companion object : ComponentType<Combat>()
}
