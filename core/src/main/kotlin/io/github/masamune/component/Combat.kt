package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag
import io.github.masamune.asset.SoundAsset
import io.github.masamune.combat.effect.DefaultEffect
import io.github.masamune.combat.effect.Effect

data class Combat(
    var effect: Effect = DefaultEffect,
    val targets: MutableEntityBag = MutableEntityBag(4),
    var attackSnd: SoundAsset = SoundAsset.ATTACK_SWIPE,
) : Component<Combat> {
    val hasEffect: Boolean
        get() = effect != DefaultEffect

    override fun type() = Combat

    fun clearEffect() {
        effect = DefaultEffect
    }

    companion object : ComponentType<Combat>()
}
