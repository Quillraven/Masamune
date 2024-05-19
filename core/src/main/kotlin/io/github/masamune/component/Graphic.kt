package io.github.masamune.component

import com.badlogic.gdx.graphics.g2d.Sprite
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Graphic(val sprite: Sprite) : Component<Graphic> {

    override fun type() = Graphic

    companion object : ComponentType<Graphic>()
}
