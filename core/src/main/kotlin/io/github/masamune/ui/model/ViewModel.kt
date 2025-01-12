package io.github.masamune.ui.model

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.asset.SoundAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.ActionExecutorService.Companion.LIFE_PER_CONST
import io.github.masamune.component.Equipment
import io.github.masamune.component.Experience
import io.github.masamune.component.Graphic
import io.github.masamune.component.Inventory
import io.github.masamune.component.Item
import io.github.masamune.component.Name
import io.github.masamune.component.Stats
import io.github.masamune.event.EventListener
import io.github.masamune.tiledmap.ItemType
import ktx.app.gdxError
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ViewModel(
    val bundle: I18NBundle,
    val audioService: AudioService,
) : EventListener {

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

    fun uiMapOf(vararg components: Component<*>): MutableMap<UIStats, String> {
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

    fun MutableMap<UIStats, String>.andEquipmentBonus(bonus: Map<UIStats, Int>): MutableMap<UIStats, String> {
        val bonusLife = bonus[UIStats.LIFE_MAX] ?: 0
        val bonusConstitution = bonus[UIStats.CONSTITUTION] ?: 0
        val bonusMana = bonus[UIStats.MANA_MAX] ?: 0
        val baseConstitution = this[UIStats.CONSTITUTION]?.toInt() ?: 0
        val baseLife = (this[UIStats.LIFE_MAX]?.toInt() ?: 0) + baseConstitution * LIFE_PER_CONST
        val baseMana = this[UIStats.MANA_MAX]?.toInt() ?: 0
        this[UIStats.LIFE_MAX] = "${(baseLife + bonusLife + bonusConstitution * LIFE_PER_CONST).toInt()}"
        this[UIStats.MANA_MAX] = "${baseMana + bonusMana}"
        return this
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

    fun playSndMenuClick() {
        audioService.play(SoundAsset.MENU_CLICK)
    }

    fun playSndMenuAccept() {
        audioService.play(SoundAsset.MENU_ACCEPT)
    }

    fun playSndMenuAbort() {
        audioService.play(SoundAsset.MENU_ABORT)
    }

    fun Vector2.toUiPosition(from: Viewport, to: Viewport): Vector2 {
        from.project(this)
        to.unproject(this)
        this.y = to.worldHeight - this.y
        return this
    }

    fun Equipment.toUiStatsMap(world: World): Map<UIStats, Int> = with(world) {
        return UIStats.entries.associateWith { uiStat ->
            this@toUiStatsMap.items
                .map { it[Stats] }
                .sumOf {
                    when (uiStat) {
                        UIStats.AGILITY -> it.agility.toInt()
                        UIStats.ARCANE_STRIKE -> (it.arcaneStrike * 100).toInt()
                        UIStats.ARMOR -> it.armor.toInt()
                        UIStats.CONSTITUTION -> it.constitution.toInt()
                        UIStats.CRITICAL_STRIKE -> (it.criticalStrike * 100).toInt()
                        UIStats.DAMAGE -> it.damage.toInt()
                        UIStats.INTELLIGENCE -> it.intelligence.toInt()
                        UIStats.LIFE_MAX -> it.lifeMax.toInt()
                        UIStats.MAGICAL_EVADE -> (it.magicalEvade * 100).toInt()
                        UIStats.MANA_MAX -> it.manaMax.toInt()
                        UIStats.PHYSICAL_EVADE -> (it.physicalEvade * 100).toInt()
                        UIStats.RESISTANCE -> it.resistance.toInt()
                        UIStats.STRENGTH -> it.strength.toInt()
                        else -> 0
                    }
                }
        }
    }

    fun Entity.toItemModel(world: World): ItemModel = with(world) {
        val itemEntity = this@toItemModel
        // and transform items into UI ItemModel objects
        val (type, cost, category, descriptionKey, _, amount) = itemEntity[Item]
        val itemName = itemEntity[Name].name
        val region: TextureRegion? = itemEntity.getOrNull(Graphic)?.region
        val itemStats = itemEntity.getOrNull(Stats) ?: io.github.masamune.tiledmap.TiledStats.NULL_STATS

        val i18nName = bundle["item.$itemName.name"]
        val i18nDescription = description(descriptionKey, itemEntity, world)
        return ItemModel(
            type = type,
            stats = itemStats,
            name = i18nName,
            cost = cost,
            description = i18nDescription,
            category = category,
            image = TextureRegionDrawable(region),
            amount = amount,
        )
    }

    fun Entity.calcEquipmentDiff(selectedItem: ItemModel, world: World): Map<UIStats, Int> {
        if (!selectedItem.category.isEquipment) {
            return emptyMap()
        }

        with(world) {
            val (items) = this@calcEquipmentDiff[Equipment]
            val itemToCompare = items.firstOrNull { it[Item].category == selectedItem.category }
            val selectedStats = selectedItem.stats

            if (itemToCompare == null) {
                // player has no item of given type selected
                // -> diff is the stats of the selected item
                return mapOf(
                    UIStats.STRENGTH to selectedStats.strength.toInt(),
                    UIStats.AGILITY to selectedStats.agility.toInt(),
                    UIStats.CONSTITUTION to selectedStats.constitution.toInt(),
                    UIStats.INTELLIGENCE to selectedStats.intelligence.toInt(),
                    UIStats.DAMAGE to selectedStats.damage.toInt(),
                    UIStats.ARMOR to selectedStats.armor.toInt(),
                    UIStats.RESISTANCE to selectedStats.resistance.toInt(),
                )
            }

            val equipStats = itemToCompare[Stats]
            if (selectedItem.type == ItemType.UNDEFINED) {
                // special unequip item -> return currently equipped item stats
                return mapOf(
                    UIStats.STRENGTH to -equipStats.strength.toInt(),
                    UIStats.AGILITY to -equipStats.agility.toInt(),
                    UIStats.CONSTITUTION to -equipStats.constitution.toInt(),
                    UIStats.INTELLIGENCE to -equipStats.intelligence.toInt(),
                    UIStats.DAMAGE to -equipStats.damage.toInt(),
                    UIStats.ARMOR to -equipStats.armor.toInt(),
                    UIStats.RESISTANCE to -equipStats.resistance.toInt(),
                )
            }

            // compare selected item with currently equipped item
            return mapOf(
                UIStats.STRENGTH to (equipStats.strength - selectedStats.strength).toInt(),
                UIStats.AGILITY to (equipStats.agility - selectedStats.agility).toInt(),
                UIStats.CONSTITUTION to (equipStats.constitution - selectedStats.constitution).toInt(),
                UIStats.INTELLIGENCE to (equipStats.intelligence - selectedStats.intelligence).toInt(),
                UIStats.DAMAGE to (equipStats.damage - selectedStats.damage).toInt(),
                UIStats.ARMOR to (equipStats.armor - selectedStats.armor).toInt(),
                UIStats.RESISTANCE to (equipStats.resistance - selectedStats.resistance).toInt(),
            )
        }
    }

    companion object {
        // format example: %STAT.MANA%
        private val DESCR_TOKEN_REGEX = "%([A-Z]+)\\.([A-Z]+)%".toRegex()
    }
}
