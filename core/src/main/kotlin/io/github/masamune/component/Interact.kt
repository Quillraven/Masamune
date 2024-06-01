package io.github.masamune.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag
import ktx.math.vec2

data class Interact(
    val entities: MutableEntityBag = MutableEntityBag(4),
    var trigger: Boolean = false,
    val lastDirection: Vector2 = vec2(),
) : Component<Interact> {
    override fun type() = Interact

    companion object : ComponentType<Interact>()
}
