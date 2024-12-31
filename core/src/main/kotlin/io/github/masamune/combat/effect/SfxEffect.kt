package io.github.masamune.combat.effect

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.asset.CachingAtlas
import io.github.masamune.component.Animation
import io.github.masamune.component.Graphic
import io.github.masamune.component.Remove
import io.github.masamune.component.Transform
import io.github.masamune.tiledmap.AnimationType

data class SfxEffect(
    override val source: Entity,
    override val target: Entity,
    private val sfxAtlas: CachingAtlas,
    private val sfxAtlasKey: String,
    private val duration: Float,
    private val scale: Float = 1f,
) : Effect {

    override fun World.onStart() {
        val (toPos, toSize, toScale) = target[Transform]
        entity {
            it += Transform(toPos.cpy().apply { z = 3f }, toSize.cpy(), toScale * scale)
            val animation = Animation.ofAtlas(sfxAtlas, sfxAtlasKey, AnimationType.IDLE)
            animation.speed = 1f / (duration / animation.gdxAnimation.animationDuration)
            animation.playMode = com.badlogic.gdx.graphics.g2d.Animation.PlayMode.NORMAL
            it += animation
            it += Graphic(animation.gdxAnimation.getKeyFrame(0f))
            it += Remove(duration)
        }
    }
}
