package io.github.masamune.ai.task

import com.github.quillraven.fleks.World
import io.github.masamune.canPerformAction
import io.github.masamune.component.Combat

class HasAttack(world: World) : FleksGuard(world) {
    override fun World.onExecute(): Boolean {
        return canPerformAction(entity, entity[Combat].attackAction)
    }
}
