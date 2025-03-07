package io.github.masamune.ai.guard

import com.badlogic.gdx.ai.btree.Task
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.system.CombatSystem
import ktx.ai.GdxAiDsl

class IsTurnMultipleOf(world: World, private val multiple: Int) : FleksGuard(world) {
    override fun World.onExecute(): Boolean {
        return world.system<CombatSystem>().turn % multiple == 0
    }
}

@GdxAiDsl
inline fun Task<Entity>.isTurnMultipleOf(
    world: World,
    multiple: Int,
    init: (@GdxAiDsl IsTurnMultipleOf).() -> Unit = {}
): Int {
    val eatTask = IsTurnMultipleOf(world, multiple)
    eatTask.init()
    return addChild(eatTask)
}
