package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Player(
    val gameProgress: Int = 0,
) : Component<Player> {
    override fun type() = Player

    companion object : ComponentType<Player>()
}
