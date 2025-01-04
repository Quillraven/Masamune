package io.github.masamune.ai

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.utils.random.ConstantFloatDistribution
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.ai.task.HasAttack
import io.github.masamune.ai.task.HasDefensiveMagic
import io.github.masamune.ai.task.HasOffensiveMagic
import io.github.masamune.ai.task.useAttack
import io.github.masamune.ai.task.useDefensiveMagic
import io.github.masamune.ai.task.useNothing
import io.github.masamune.ai.task.useOffensiveMagic
import ktx.ai.GdxAiRandom
import ktx.ai.GdxAiSequence
import ktx.ai.behaviorTree
import ktx.ai.selector
import ktx.ai.sequence

fun defaultBehavior(
    world: World,
    entity: Entity,
    offensiveMagicChange: Float = 0.2f,
    defensiveMagicChange: Float = 0.2f,
): BehaviorTree<Entity> = behaviorTree {
    `object` = entity

    selector {
        sequence {
            guard = GdxAiSequence(
                GdxAiRandom(ConstantFloatDistribution(offensiveMagicChange)),
                HasOffensiveMagic(world)
            )
            useOffensiveMagic(world)
        }

        sequence {
            guard = GdxAiSequence(
                GdxAiRandom(ConstantFloatDistribution(defensiveMagicChange)),
                HasDefensiveMagic(world)
            )
            useDefensiveMagic(world)
        }

        sequence {
            guard = GdxAiSequence(HasAttack(world))
            useAttack(world)
        }

        useNothing(world)
    }
}
