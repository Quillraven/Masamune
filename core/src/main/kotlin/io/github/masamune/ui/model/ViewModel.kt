package io.github.masamune.ui.model

import com.badlogic.gdx.utils.I18NBundle
import io.github.masamune.component.Experience
import io.github.masamune.component.Inventory
import io.github.masamune.component.Stats
import io.github.masamune.event.EventListener
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ViewModel : EventListener {

    @PublishedApi
    internal val actionsMap = mutableMapOf<KProperty<*>, MutableList<(Any) -> Unit>>()

    inline fun <reified T : Any> propertyNotify(initialValue: T): ReadWriteProperty<ViewModel, T> =
        Delegates.observable(initialValue) { property, _, newValue -> notify(property, newValue) }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> onPropertyChange(property: KProperty<T>, noinline action: (T) -> Unit) {
        val actions = actionsMap.getOrPut(property) { mutableListOf() } as MutableList<(T) -> Unit>
        actions += action
    }

    fun notify(property: KProperty<*>, value: Any) {
        actionsMap[property]?.forEach { action -> action(value) }
    }

    fun Stats.toUiMap(bundle: I18NBundle): MutableMap<UIStats, Pair<String, String>> {
        val stats = this.tiledStats
        return mutableMapOf(
            UIStats.AGILITY to (bundle["stats.agility"] to "${stats.agility.toInt()}"),
            UIStats.ARCANE_STRIKE to (bundle["stats.arcaneStrike"] to "${(stats.arcaneStrike * 100).toInt()}%"),
            UIStats.ARMOR to (bundle["stats.armor"] to "${stats.armor.toInt()}"),
            UIStats.CONSTITUTION to (bundle["stats.constitution"] to "${stats.constitution.toInt()}"),
            UIStats.CRITICAL_STRIKE to (bundle["stats.criticalStrike"] to "${(stats.criticalStrike * 100).toInt()}%"),
            UIStats.ATTACK to (bundle["stats.attack"] to "${stats.damage.toInt()}"),
            UIStats.INTELLIGENCE to (bundle["stats.intelligence"] to "${stats.intelligence.toInt()}"),
            UIStats.LIFE to (bundle["stats.life"] to "${stats.life.toInt()}"),
            UIStats.LIFE_MAX to (bundle["stats.life"] to "${stats.lifeMax.toInt()}"),
            UIStats.MAGICAL_EVADE to (bundle["stats.magicalEvade"] to "${(stats.magicalEvade * 100).toInt()}%"),
            UIStats.MANA to (bundle["stats.mana"] to "${stats.mana.toInt()}"),
            UIStats.MANA_MAX to (bundle["stats.mana"] to "${stats.manaMax.toInt()}"),
            UIStats.PHYSICAL_EVADE to (bundle["stats.physicalEvade"] to "${(stats.physicalEvade * 100).toInt()}%"),
            UIStats.RESISTANCE to (bundle["stats.resistance"] to "${stats.resistance.toInt()}"),
            UIStats.STRENGTH to (bundle["stats.strength"] to "${stats.strength.toInt()}"),
        )
    }

    fun Experience.toUiMap(bundle: I18NBundle): MutableMap<UIStats, Pair<String, String>> {
        return mutableMapOf(
            UIStats.LEVEL to (bundle["stats.level"] to "${this.level}"),
            UIStats.XP to (bundle["stats.xp"] to "${this.current}"),
            UIStats.XP_NEEDED to (bundle["stats.xpNeeded"] to "${this.forLevelUp}"),
        )
    }

    fun Inventory.toUiMap(bundle: I18NBundle): MutableMap<UIStats, Pair<String, String>> {
        return mutableMapOf(
            UIStats.TALONS to (bundle["stats.talons"] to "${this.talons}"),
        )
    }

    infix fun MutableMap<UIStats, Pair<String, String>>.and(other: Map<UIStats, Pair<String, String>>): MutableMap<UIStats, Pair<String, String>> {
        this.putAll(other)
        return this
    }
}
