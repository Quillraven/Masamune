package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag
import io.github.masamune.combat.ActionEffect
import io.github.masamune.combat.DefaultActionEffect

data class Combat(
    var effect: ActionEffect = DefaultActionEffect,
    val targets: MutableEntityBag = MutableEntityBag(4),
) : Component<Combat> {
    val hasEffect: Boolean
        get() = effect != DefaultActionEffect

    override fun type() = Combat

    fun clearEffect() {
        effect = DefaultActionEffect
    }

    companion object : ComponentType<Combat>()
}
