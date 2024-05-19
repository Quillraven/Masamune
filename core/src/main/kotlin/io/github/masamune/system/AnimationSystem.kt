package io.github.masamune.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.masamune.component.Animation
import io.github.masamune.component.Graphic

class AnimationSystem : IteratingSystem(family { all(Animation, Graphic) }) {

    override fun onTickEntity(entity: Entity) = with(entity[Animation]) {
        // update animation
        stateTime += deltaTime * speed
        val keyFrame = gdxAnimation.getKeyFrame(stateTime)

        // update graphic region
        entity[Graphic].region = keyFrame
    }

}
