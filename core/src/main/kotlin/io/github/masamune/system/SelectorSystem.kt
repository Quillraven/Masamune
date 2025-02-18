package io.github.masamune.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.masamune.component.Selector
import io.github.masamune.component.Transform

class SelectorSystem : IteratingSystem(family { all(Selector, Transform) }) {

    override fun onTickEntity(entity: Entity) {
        val (target) = entity[Selector]
        val (targetPos, targetSize) = target[Transform]
        val (selectorPos, selectorSize) = entity[Transform]
        selectorPos.set(targetPos.x, targetPos.y, 5f)
        selectorSize.set(targetSize)
    }

}
