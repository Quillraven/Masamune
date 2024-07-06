package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Portal(
    val toMap: String,
    val toPortalId: Int,
) : Component<Portal> {
    override fun type() = Portal

    companion object : ComponentType<Portal>()
}
