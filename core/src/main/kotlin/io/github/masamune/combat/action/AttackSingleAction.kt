package io.github.masamune.combat.action

import io.github.masamune.combat.ActionExecutorService

class AttackSingleAction : Action(ActionTargetType.SINGLE, manaCost = 0) {
    override fun ActionExecutorService.onStart() {
        attack(target = singleTarget)
        endAction()
    }
}
