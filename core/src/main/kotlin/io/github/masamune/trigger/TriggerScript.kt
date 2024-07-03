package io.github.masamune.trigger

import com.github.quillraven.fleks.World

class TriggerScript(
    val name: String,
    val world: World,
    @PublishedApi
    internal val actions: MutableList<TriggerAction>
) {

    val numActions: Int
        get() = actions.size

    init {
        actions.first().run { world.onStart() }
    }

    inline fun <reified T : TriggerAction> getAction(actionIdx: Int): T {
        return actions[actionIdx] as T
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

    override fun toString(): String {
        return "TriggerScript(name='$name', numActions=$numActions)"
    }

}
