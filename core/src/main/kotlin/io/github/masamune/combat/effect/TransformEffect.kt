package io.github.masamune.combat.effect

import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Animation
import io.github.masamune.component.Fade
import io.github.masamune.component.Remove
import io.github.masamune.tiledmap.TiledObjectType
import io.github.masamune.tiledmap.TiledService

data class TransformEffect(
    override val source: Entity,
    private val toType: TiledObjectType,
    private val spawnX: Float,
    private val spawnY: Float,
    private val transformTime: Float,
) : Effect {
    override val target: Entity = Entity.NONE

    override fun World.onStart() {
        val enemy = inject<TiledService>().loadCombatEnemy(this, toType, spawnX, spawnY)
        enemy[Animation].speed = 0.4f

        enemy.configure {
            it += Fade(0f, 1f, transformTime, Interpolation.fade)
        }
        source.configure {
            it += Fade(1f, 0f, transformTime * 0.75f, Interpolation.fade)
            it += Remove(transformTime * 0.75f)
        }
    }

}
