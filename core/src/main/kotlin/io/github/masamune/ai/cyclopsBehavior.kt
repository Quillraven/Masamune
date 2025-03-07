package io.github.masamune.ai

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.ai.guard.canPerform
import io.github.masamune.ai.guard.hasAttack
import io.github.masamune.ai.guard.isLifeLowerThan
import io.github.masamune.ai.guard.isTurnMultipleOf
import io.github.masamune.ai.task.perform
import io.github.masamune.ai.task.useAttack
import io.github.masamune.ai.task.useNothing
import io.github.masamune.tiledmap.ActionType
import ktx.ai.behaviorTree
import ktx.ai.selector
import ktx.ai.sequence

fun cyclopsBehavior(
    world: World,
    entity: Entity,
): BehaviorTree<Entity> = behaviorTree {
    `object` = entity

    selector {
        // regenerate once when below 30% life
        sequence {
            isLifeLowerThan(world, 0.3f)
            canPerform(world, ActionType.HEAL)
            perform(world, ActionType.HEAL)
        }

        // cast DEMI once when below 10% life
        sequence {
            isLifeLowerThan(world, 0.1f)
            canPerform(world, ActionType.FIREBALL)
            perform(world, ActionType.FIREBALL)
        }

        // perform double attack every 4 turns
        sequence {
            isTurnMultipleOf(world, 4)
            canPerform(world, ActionType.FIREBOLT)
            perform(world, ActionType.FIREBOLT)
        }

        // perform slow every 5 turns
        // double attack has priority if we have a turn where slow and double attack shall be performed
        sequence {
            isTurnMultipleOf(world, 5)
            canPerform(world, ActionType.FIREBOLT)
            perform(world, ActionType.FIREBOLT)
        }

        // otherwise, just attack
        sequence {
            hasAttack(world)
            useAttack(world)
        }

        useNothing(world)
    }
}
