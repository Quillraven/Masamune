package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Name(val name: String) : Component<Name> {
    override fun type() = Name

    companion object : ComponentType<Name>()
}
