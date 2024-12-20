package io.github.masamune.combat.effect

import io.github.masamune.combat.Action
import io.github.masamune.combat.ActionTargetType

class SingleAttackEffect : Effect(ActionTargetType.SINGLE) {
    override fun Action.onStart() {
        attack(target = singleTarget)
        endAction()
    }
}
