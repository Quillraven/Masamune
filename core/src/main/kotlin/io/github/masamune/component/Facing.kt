package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType


enum class FacingDirection {
    UNDEFINED, DOWN, LEFT, RIGHT, UP;

    val atlasKey: String = this.name.lowercase()
}

data class Facing(
    var direction: FacingDirection,
    var lastDirection: FacingDirection = direction,
) : Component<Facing> {

    fun hasChanged(): Boolean = direction != lastDirection

    override fun type() = Facing

    companion object : ComponentType<Facing>()
}
