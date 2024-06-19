package io.github.masamune.ui.model

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class PropertyChangeSource {
    @PublishedApi
    internal val actionsMap = mutableMapOf<KProperty<*>, MutableList<(Any) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> onPropertyChange(property: KProperty<T>, noinline action: (T) -> Unit) {
        val actions = actionsMap.getOrPut(property) { mutableListOf() } as MutableList<(T) -> Unit>
        actions += action
    }

    fun notify(property: KProperty<*>, value: Any) {
        actionsMap[property]?.forEach { action -> action(value) }
    }
}

class PropertyNotifier<T : Any>(initialValue: T) : ReadWriteProperty<PropertyChangeSource, T> {
    private var _value: T = initialValue

    override operator fun getValue(thisRef: PropertyChangeSource, property: KProperty<*>): T = _value

    override operator fun setValue(thisRef: PropertyChangeSource, property: KProperty<*>, value: T) {
        _value = value
        thisRef.notify(property, value)
    }
}

inline fun <reified T : Any> propertyNotify(initialValue: T): PropertyNotifier<T> = PropertyNotifier(initialValue)
