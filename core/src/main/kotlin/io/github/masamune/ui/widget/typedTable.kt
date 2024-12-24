package io.github.masamune.ui.widget

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

@Suppress("UNCHECKED_CAST")
open class TypedTable<T : Actor>(skin: Skin) : Table(skin), KTable {

    operator fun get(index: Int): T = this.children[index] as T

    inline fun forEach(action: (T) -> Unit) {
        children.forEach { action(it as T) }
    }

    fun first(predicate: (T) -> Boolean): T = children.first { predicate(it as T) } as T

    fun count(predicate: (T) -> Boolean): Int = children.count { predicate(it as T) }

}

@Scene2dDsl
inline fun <reified T : Actor> KWidget<Actor>.typedTable(
    skin: Skin,
    init: TypedTable<T>.(Actor) -> Unit = {},
): TypedTable<T> {
    return actor(TypedTable<T>(skin), init)
}
