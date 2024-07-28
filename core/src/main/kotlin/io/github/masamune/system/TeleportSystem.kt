package io.github.masamune.system

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.masamune.component.Physic
import io.github.masamune.component.Teleport
import io.github.masamune.component.Transform
import ktx.box2d.body
import ktx.log.logger

class TeleportSystem : IteratingSystem(family { all(Teleport, Physic) }) {

    override fun onTickEntity(entity: Entity) = with(entity[Physic]) {
        val toPosition = entity[Teleport].toPosition

        // teleport physic body to new location by destroying it and recreating it at the new location
        log.debug { "Teleporting entity $entity to $toPosition" }
        body = body.cpy(toPosition)
        prevPosition.set(toPosition)
        entity.getOrNull(Transform)?.position?.set(body.position, 0f)

        entity.configure { it -= Teleport }
    }

    private fun Body.cpy(newPosition: Vector2): Body {
        val origBody = this@cpy
        val physicWorld = origBody.world

        val newBody = physicWorld.body(origBody.type) {
            position.set(newPosition.x, newPosition.y)
            fixedRotation = origBody.isFixedRotation
            userData = origBody.userData
        }

        for (oldFixture in origBody.fixtureList) {
            newBody.createFixture(oldFixture.shape, oldFixture.density).run {
                userData = oldFixture.userData
                isSensor = oldFixture.isSensor
                friction = oldFixture.friction
                restitution = oldFixture.restitution
            }
        }

        physicWorld.destroyBody(origBody)
        return newBody
    }

    companion object {
        private val log = logger<TeleportSystem>()
    }

}
