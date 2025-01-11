package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.masamune.combat.ActionExecutorService.Companion.LIFE_PER_CONST
import io.github.masamune.tiledmap.TiledStats

class Stats(
    agility: Float = 0f,
    arcaneStrike: Float = 0f,
    armor: Float = 0f,
    constitution: Float = 0f,
    criticalStrike: Float = 0f,
    damage: Float = 0f,
    intelligence: Float = 0f,
    life: Float = 0f,
    lifeMax: Float = 0f,
    magicalEvade: Float = 0f,
    mana: Float = 0f,
    manaMax: Float = 0f,
    physicalEvade: Float = 0f,
    resistance: Float = 0f,
    strength: Float = 0f,
    val percModifier: TiledStats = TiledStats(),
) : Component<Stats>, TiledStats(
    agility = agility,
    arcaneStrike = arcaneStrike,
    armor = armor,
    constitution = constitution,
    criticalStrike = criticalStrike,
    damage = damage,
    intelligence = intelligence,
    life = life,
    lifeMax = lifeMax,
    magicalEvade = magicalEvade,
    mana = mana,
    manaMax = manaMax,
    physicalEvade = physicalEvade,
    resistance = resistance,
    strength = strength,
) {

    init {
        if (life == lifeMax) {
            // Total life might be different due to constitution and other bonuses.
            // If life should be the same as lifeMax then set life to its correct value (=total life including bonus).
            this.life = totalLifeMax
        }
    }

    // there is no total mana/life by design because such bonuses must be one manaMax/lifeMax
    val totalAgility: Float
        get() = agility * (1f + percModifier.agility)
    val totalArcaneStrike: Float
        get() = arcaneStrike * (1f + percModifier.arcaneStrike)
    val totalArmor: Float
        get() = armor * (1f + percModifier.armor)
    val totalConstitution: Float
        get() = constitution * (1f + percModifier.constitution)
    val totalCriticalStrike: Float
        get() = criticalStrike * (1f + percModifier.criticalStrike)
    val totalDamage: Float
        get() = damage * (1f + percModifier.damage)
    val totalIntelligence: Float
        get() = intelligence * (1f + percModifier.intelligence)
    val totalLifeMax: Float
        get() = (lifeMax * (1f + percModifier.lifeMax)) + (totalConstitution * LIFE_PER_CONST)
    val totalMagicalEvade: Float
        get() = magicalEvade * (1f + percModifier.magicalEvade)
    val totalManaMax: Float
        get() = manaMax * (1f + percModifier.manaMax)
    val totalPhysicalEvade: Float
        get() = physicalEvade * (1f + percModifier.physicalEvade)
    val totalResistance: Float
        get() = resistance * (1f + percModifier.resistance)
    val totalStrength: Float
        get() = strength * (1f + percModifier.strength)

    override fun type() = Stats

    fun withBonus(bonus: Stats): Stats {
        this += bonus
        return this
    }

    operator fun plusAssign(other: Stats) {
        // there is no mana/life by design because such bonuses must be on manaMax/lifeMax
        // flat bonus
        this.agility += other.agility
        this.arcaneStrike += other.arcaneStrike
        this.armor += other.armor
        this.constitution += other.constitution
        this.criticalStrike += other.criticalStrike
        this.damage += other.damage
        this.intelligence += other.intelligence
        this.lifeMax += other.lifeMax
        this.magicalEvade += other.magicalEvade
        this.manaMax += other.manaMax
        this.physicalEvade += other.physicalEvade
        this.resistance += other.resistance
        this.strength += other.strength

        // percentage bonus
        this.percModifier.agility += other.percModifier.agility
        this.percModifier.arcaneStrike += other.percModifier.arcaneStrike
        this.percModifier.armor += other.percModifier.armor
        this.percModifier.constitution += other.percModifier.constitution
        this.percModifier.criticalStrike += other.percModifier.criticalStrike
        this.percModifier.damage += other.percModifier.damage
        this.percModifier.intelligence += other.percModifier.intelligence
        this.percModifier.lifeMax += other.percModifier.lifeMax
        this.percModifier.magicalEvade += other.percModifier.magicalEvade
        this.percModifier.manaMax += other.percModifier.manaMax
        this.percModifier.physicalEvade += other.percModifier.physicalEvade
        this.percModifier.resistance += other.percModifier.resistance
        this.percModifier.strength += other.percModifier.strength
    }

    companion object : ComponentType<Stats>() {
        fun of(tiledStats: TiledStats): Stats = Stats(
            agility = tiledStats.agility,
            arcaneStrike = tiledStats.arcaneStrike,
            armor = tiledStats.armor,
            constitution = tiledStats.constitution,
            criticalStrike = tiledStats.criticalStrike,
            damage = tiledStats.damage,
            intelligence = tiledStats.intelligence,
            life = tiledStats.life,
            lifeMax = tiledStats.lifeMax,
            magicalEvade = tiledStats.magicalEvade,
            mana = tiledStats.mana,
            manaMax = tiledStats.manaMax,
            physicalEvade = tiledStats.physicalEvade,
            resistance = tiledStats.resistance,
            strength = tiledStats.strength,
        )
    }
}
