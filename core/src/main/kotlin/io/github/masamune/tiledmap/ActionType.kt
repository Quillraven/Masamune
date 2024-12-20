package io.github.masamune.tiledmap

import io.github.masamune.combat.action.*

// This is an autogenerated class by gradle's 'genTiledEnumsAndExtensions' task. Do not touch it!
enum class ActionType(private val actionFactory: () -> Action) {
    ATTACK_SINGLE(::AttackSingleAction),
    FIREBALL(::FireballAction),
    FIREBOLT(::FireboltAction),
    UNDEFINED({ DefaultAction }),
    ;

    operator fun invoke() = actionFactory()
}
