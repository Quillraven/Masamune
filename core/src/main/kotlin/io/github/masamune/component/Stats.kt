package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.masamune.tiledmap.TiledStats

data class Stats(val tiledStats: TiledStats) : Component<Stats> {
    override fun type() = Stats

    companion object : ComponentType<Stats>()
}

