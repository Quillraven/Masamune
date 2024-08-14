package io.github.masamune.system

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.masamune.component.*
import io.github.masamune.component.Animation.Companion.DEFAULT_FRAME_DURATION
import ktx.app.gdxError

class AnimationSystem : IteratingSystem(family { all(Animation, Graphic) }) {

    override fun onTickEntity(entity: Entity) = with(entity[Animation]) {
        if (changeTo != AnimationType.UNKNOWN) {
            // change animation and reset state timer
            val currentAnimationName = gdxAnimation.keyFrames.first().name
            gdxAnimation = gdxAnimation(atlas, currentAnimationName, changeTo)
            stateTime = 0f
            changeTo = AnimationType.UNKNOWN
        } else if (changeFacingTo != FacingDirection.UNKNOWN) {
            val currentAnimationName = gdxAnimation.keyFrames.first().name
            gdxAnimation = gdxAnimation(atlas, currentAnimationName, changeFacingTo)
            stateTime = 0f
            changeFacingTo = FacingDirection.UNKNOWN
        } else {
            // update animation
            stateTime += deltaTime * speed
        }

        // update graphic region
        val keyFrame = gdxAnimation.getKeyFrame(stateTime)
        entity[Graphic].region = keyFrame
    }

    // TODO
    //  1) cache animations (add this logic to AssetService and reuse it in TiledService)
    //  2) optimize atlas logging (e.g. map from TextureAtlas to AtlasAsset in AssetService)
    //  3) Refactor code below to avoid creating so many strings
    //     maybe add mainKey to Animation component
    //       subkey is the AnimationType PLUS facing
    //  4) refactor Tiled -> instead of hasAnimation we can set the AnimationType
    //     it is then either animationType or textureAtlasKey
    //  5) refactor global state logic with previous direction stuff
    private fun gdxAnimation(atlas: TextureAtlas, currentAnimationName: String, changeTo: AnimationType): GdxAnimation {
        val atlasMainKey = currentAnimationName.substringBefore("/")
        val atlasSubKey = changeTo.atlasKey
        val atlasRegionKey = "$atlasMainKey/${atlasSubKey}_down"
        val texRegions = atlas.findRegions(atlasRegionKey)
        if (texRegions.isEmpty) {
            gdxError("No regions in $atlas for key $atlasRegionKey")
        }

        return GdxAnimation(DEFAULT_FRAME_DURATION, texRegions, PlayMode.LOOP)
    }

    private fun gdxAnimation(atlas: TextureAtlas, currentAnimationName: String, newFacing: FacingDirection): GdxAnimation {
        val currentKeyNoFacing = currentAnimationName.substringBefore("_")
        val atlasRegionKey = "${currentKeyNoFacing}_${newFacing.atlasKey}"
        val texRegions = atlas.findRegions(atlasRegionKey)
        if (texRegions.isEmpty) {
            gdxError("No regions in $atlas for key $atlasRegionKey")
        }

        return GdxAnimation(DEFAULT_FRAME_DURATION, texRegions, PlayMode.LOOP)
    }

}
