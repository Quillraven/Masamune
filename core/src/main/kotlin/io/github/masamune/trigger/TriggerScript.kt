package io.github.masamune.trigger

import com.github.quillraven.fleks.World

class TriggerScript(
    val name: String,
    val world: World,
    private val actions: MutableList<TriggerAction>
) {

    init {
        actions.first().run { world.onStart() }
    }

    fun onUpdate(): Boolean {
        actions.first().run {
            if (world.onUpdate()) {
                // action finished -> go to next action or end script if there are no other actions
                actions.removeFirst()
                if (actions.isEmpty()) {
                    return true
                }
                actions.first().run { world.onStart() }
            }
        }

        return false
    }
}
