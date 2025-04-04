package io.github.masamune.ai.guard

import com.badlogic.gdx.ai.btree.Task
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.canPerformAction
import io.github.masamune.combat.action.isHealing
import io.github.masamune.component.Combat
import ktx.ai.GdxAiDsl

class HasHealMagic(world: World) : FleksGuard(world) {
    override fun World.onExecute(): Boolean {
        return entity[Combat].magicActions
            .filter { it.isHealing }
            .any { canPerformAction(entity, it) }
    }
}

@GdxAiDsl
inline fun Task<Entity>.hasHealMagic(
    world: World,
    init: (@GdxAiDsl HasHealMagic).() -> Unit = {}
): Int {
    val eatTask = HasHealMagic(world)
    eatTask.init()
    return addChild(eatTask)
}
