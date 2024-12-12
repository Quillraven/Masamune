package io.github.masamune.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
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
    override fun type() = Stats

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

fun World.isEntityDead(entity: Entity): Boolean = with(this) {
    return entity[Stats].life <= 0f
}
