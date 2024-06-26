package io.github.masamune

import com.badlogic.gdx.physics.box2d.*
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Player
import io.github.masamune.event.EventService
import io.github.masamune.event.PlayerInteractBeginContactEvent
import io.github.masamune.event.PlayerInteractEndContactEvent

private interface ContactAdapter : ContactListener {
    override fun beginContact(contact: Contact) = Unit

    override fun endContact(contact: Contact) = Unit

    override fun preSolve(contact: Contact, oldManifold: Manifold) = Unit

    override fun postSolve(contact: Contact, impulse: ContactImpulse) = Unit
}

class PhysicContactHandler(
    private val eventService: EventService,
    private val world: World,
) : ContactAdapter {
    override fun beginContact(contact: Contact) {
        if (!contact.isPlayerInteract()) {
            return
        }

        eventService.fire(PlayerInteractBeginContactEvent(contact.playerEntity, contact.interactEntity))
    }

    override fun endContact(contact: Contact) {
        if (!contact.isPlayerInteract()) {
            return
        }

        eventService.fire(PlayerInteractEndContactEvent(contact.playerEntity, contact.interactEntity))
    }

    private fun Contact.isPlayerInteract(): Boolean {
        return (fixtureA.isInteract() && fixtureA.isPlayerEntity() && fixtureB.isNonPlayerEntity())
            || (fixtureB.isInteract() && fixtureB.isPlayerEntity() && fixtureA.isNonPlayerEntity())
    }

    private fun Fixture.isInteract(): Boolean {
        return "interact" == userData
    }

    private fun Fixture.isPlayerEntity(): Boolean = with(world) {
        val userData = body.userData
        return userData is Entity && userData has Player
    }

    private fun Fixture.isNonPlayerEntity(): Boolean = with(world) {
        val userData = body.userData
        return userData is Entity && userData hasNo Player
    }

    private val Contact.playerEntity: Entity
        get() = if (fixtureA.isPlayerEntity()) fixtureA.body.userData as Entity else fixtureB.body.userData as Entity

    private val Contact.interactEntity: Entity
        get() = if (fixtureA.isNonPlayerEntity()) fixtureA.body.userData as Entity else fixtureB.body.userData as Entity
}
