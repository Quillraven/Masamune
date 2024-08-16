package io.github.masamune.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.masamune.component.Animation
import io.github.masamune.component.Facing
import io.github.masamune.component.FacingDirection
import io.github.masamune.component.Graphic
import io.github.masamune.tiledmap.AnimationType

class AnimationSystem : IteratingSystem(family { all(Animation, Graphic) }) {

    override fun onTickEntity(entity: Entity) = with(entity[Animation]) {
        if (changeTo != AnimationType.UNDEFINED) {
            // change animation and reset state timer
            gdxAnimation = atlas.gdxAnimation(atlasKey, changeTo, entity[Facing].direction)
            stateTime = 0f
            animationType = changeTo
            changeTo = AnimationType.UNDEFINED
        } else if (changeFacingTo != FacingDirection.UNDEFINED) {
            // change animation and reset state timer
            gdxAnimation = atlas.gdxAnimation(atlasKey, animationType, changeFacingTo)
            stateTime = 0f
            changeFacingTo = FacingDirection.UNDEFINED
        } else {
            // update animation
            stateTime += deltaTime * speed
        }

        // update graphic region
        val keyFrame = gdxAnimation.getKeyFrame(stateTime)
        entity[Graphic].region = keyFrame
    }

}
