package io.github.masamune.component

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.masamune.asset.CachingAtlas
import io.github.masamune.tiledmap.AnimationType

typealias GdxAnimation = com.badlogic.gdx.graphics.g2d.Animation<AtlasRegion>

data class Animation(
    val atlas: CachingAtlas,
    val atlasKey: String,
    var animationType: AnimationType,
    var gdxAnimation: GdxAnimation,
    var stateTime: Float = 0f,
    var speed: Float = 1f,
    var changeTo: AnimationType = AnimationType.UNDEFINED,
    var changeFacingTo: FacingDirection = FacingDirection.UNDEFINED,
) : Component<Animation> {
    override fun type() = Animation

    companion object : ComponentType<Animation>() {
        const val DEFAULT_FRAME_DURATION = 1 / 5f

        fun ofAtlas(
            atlas: CachingAtlas,
            mainKey: String,
            animationType: AnimationType,
            direction: FacingDirection = FacingDirection.UNDEFINED,
            speed: Float = 1f
        ): Animation {
            val gdxAnimation = atlas.gdxAnimation(mainKey, animationType, direction)
            return Animation(atlas, mainKey, animationType, gdxAnimation, speed = speed)
        }
    }
}
