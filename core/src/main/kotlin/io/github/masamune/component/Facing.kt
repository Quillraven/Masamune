package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType


enum class FacingDirection {
    UNDEFINED, DOWN, LEFT, RIGHT, UP;

    val atlasKey: String = this.name.lowercase()
}

data class Facing(var direction: FacingDirection) : Component<Facing> {
    override fun type() = Facing

    companion object : ComponentType<Facing>()
}
