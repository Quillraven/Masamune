package io.github.masamune.component

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.app.gdxError

typealias GdxAnimation = com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>

data class Animation(
    var gdxAnimation: GdxAnimation,
    var stateTime: Float = 0f,
    var speed: Float = 1f,
) : Component<Animation> {
    override fun type() = Animation

    companion object : ComponentType<Animation>() {
        private const val DEFAULT_FRAME_DURATION = 1 / 12f

        fun ofAtlasRegions(
            atlas: TextureAtlas,
            regionName: String,
            playMode: PlayMode = PlayMode.LOOP,
            speed: Float = 1f
        ): Animation {
            val regions = atlas.findRegions(regionName)
            if (regions.isEmpty) {
                gdxError("There are no regions with name $regionName in atlas $atlas")
            }

            val gdxAnimation = GdxAnimation(DEFAULT_FRAME_DURATION, regions, playMode)
            return Animation(gdxAnimation, speed = speed)
        }
    }
}
