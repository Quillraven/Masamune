package io.github.masamune.ui.model

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Experience
import io.github.masamune.component.Inventory
import io.github.masamune.component.Stats
import io.github.masamune.event.EventListener
import ktx.app.gdxError
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ViewModel(val bundle: I18NBundle) : EventListener {

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

    fun uiMapOf(vararg components: Component<*>): Map<UIStats, String> {
        val result = mutableMapOf<UIStats, String>()

        components.forEach { component ->
            when (component) {
                is Stats -> {
                    result += UIStats.AGILITY to "${component.agility.toInt()}"
                    result += UIStats.ARCANE_STRIKE to "${(component.arcaneStrike * 100).toInt()}%"
                    result += UIStats.ARMOR to "${component.armor.toInt()}"
                    result += UIStats.CONSTITUTION to "${component.constitution.toInt()}"
                    result += UIStats.CRITICAL_STRIKE to "${(component.criticalStrike * 100).toInt()}%"
                    result += UIStats.DAMAGE to "${component.damage.toInt()}"
                    result += UIStats.INTELLIGENCE to "${component.intelligence.toInt()}"
                    result += UIStats.LIFE to "${component.life.toInt()}"
                    result += UIStats.LIFE_MAX to "${component.lifeMax.toInt()}"
                    result += UIStats.MAGICAL_EVADE to "${(component.magicalEvade * 100).toInt()}%"
                    result += UIStats.MANA to "${component.mana.toInt()}"
                    result += UIStats.MANA_MAX to "${component.manaMax.toInt()}"
                    result += UIStats.PHYSICAL_EVADE to "${(component.physicalEvade * 100).toInt()}%"
                    result += UIStats.RESISTANCE to "${component.resistance.toInt()}"
                    result += UIStats.STRENGTH to "${component.strength.toInt()}"
                }

                is Experience -> {
                    result += UIStats.LEVEL to "${component.level}"
                    result += UIStats.XP to "${component.current}"
                    result += UIStats.XP_NEEDED to "${component.forLevelUp}"
                }

                is Inventory -> {
                    result += UIStats.TALONS to "${component.talons}"
                }

                else -> gdxError("Unsupported component: $component")
            }
        }

        return result
    }

    fun i18nTxt(key: I18NKey): String = bundle[key]

    // function to get game object descriptions like for items.
    // It detects special strings like %STAT.MANA% and replaces it with the proper entity data
    fun description(key: String, entity: Entity, world: World): String {
        // user format method of bundle to correctly replace colors like {{COLOR=ROYAL}}
        val description = bundle.format(key)
        return description.replace(DESCR_TOKEN_REGEX) {
            when (it.groupValues[1]) {
                "STAT" -> replaceStatToken(it.groupValues[2], entity, world)
                else -> gdxError("Unsupported description token in key $key: ${it.groupValues.first()}")
            }
        }
    }

    private fun replaceStatToken(statName: String, entity: Entity, world: World): String = with(world) {
        val stats = entity[Stats]
        when (statName) {
            "AGI" -> "${stats.agility.toInt()}"
            "ARCSTR" -> "${(stats.arcaneStrike * 100).toInt()}%"
            "ARMOR" -> "${stats.armor.toInt()}"
            "CONST" -> "${stats.constitution.toInt()}"
            "CRISTR" -> "${(stats.criticalStrike * 100).toInt()}%"
            "DAMAGE" -> "${stats.damage.toInt()}"
            "INT" -> "${stats.intelligence.toInt()}"
            "LIFE" -> "${stats.life.toInt()}"
            "MAXLIFE" -> "${stats.lifeMax.toInt()}"
            "MAGEVA" -> "${(stats.magicalEvade * 100).toInt()}%"
            "MANA" -> "${stats.mana.toInt()}"
            "MAXMANA" -> "${stats.manaMax.toInt()}"
            "PHYEVA" -> "${(stats.physicalEvade * 100).toInt()}%"
            "RES" -> "${stats.resistance.toInt()}"
            "STR" -> "${stats.strength.toInt()}"
            else -> gdxError("Unsupported stat: $statName")
        }
    }

    companion object {
        // format example: %STAT.MANA%
        private val DESCR_TOKEN_REGEX = "%([A-Z]+)\\.([A-Z]+)%".toRegex()
    }
}
