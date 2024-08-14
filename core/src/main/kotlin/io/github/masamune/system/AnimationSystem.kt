package io.github.masamune.system

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.component.Animation
import io.github.masamune.component.Animation.Companion.DEFAULT_FRAME_DURATION
import io.github.masamune.component.AnimationType
import io.github.masamune.component.GdxAnimation
import io.github.masamune.component.Graphic
import ktx.app.gdxError

class AnimationSystem(
    assetService: AssetService = inject<AssetService>(),
    private val atlasCharsAndProps: TextureAtlas = assetService[AtlasAsset.CHARS_AND_PROPS],
) : IteratingSystem(family { all(Animation, Graphic) }) {

    override fun onTickEntity(entity: Entity) = with(entity[Animation]) {
        if (changeTo != AnimationType.UNKNOWN) {
            // change animation and reset state timer
            val currentAnimationName = gdxAnimation.keyFrames.first().name
            gdxAnimation = gdxAnimation(currentAnimationName, changeTo)
            stateTime = 0f
            changeTo = AnimationType.UNKNOWN
        } else {
            // update animation
            stateTime += deltaTime * speed
        }

        // update graphic region
        val keyFrame = gdxAnimation.getKeyFrame(stateTime)
        entity[Graphic].region = keyFrame
    }

    // TODO
    //  1) cache animations
    //  2) add facing component? Or calculate it out of move direction?
    //  3) animation also needs an update if facing changes -> how to solve that? ;)
    //  4) can we not hardcode the atlas that is used below for findRegions?
    private fun gdxAnimation(currentAnimationName: String, changeTo: AnimationType): GdxAnimation {
        val atlasMainKey = currentAnimationName.substringBefore("/")
        val atlasSubKey = changeTo.atlasKey
        val atlasRegionKey = "$atlasMainKey/${atlasSubKey}_down"
        val texRegions = atlasCharsAndProps.findRegions(atlasRegionKey)
        if (texRegions.isEmpty) {
            gdxError("No regions in $atlasCharsAndProps for key $atlasRegionKey")
        }

        return GdxAnimation(DEFAULT_FRAME_DURATION, texRegions, PlayMode.LOOP)
    }

}
