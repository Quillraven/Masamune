package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.masamune.tiledmap.TiledObjectType

data class Tiled(
    val id: Int,
    val objType: TiledObjectType,
) : Component<Tiled> {
    override fun type() = Tiled

    companion object : ComponentType<Tiled>()
}
