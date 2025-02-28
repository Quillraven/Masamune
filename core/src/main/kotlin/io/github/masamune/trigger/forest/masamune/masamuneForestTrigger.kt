package io.github.masamune.trigger.forest.masamune

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.trigger.TriggerScript
import io.github.masamune.trigger.trigger

fun World.masamuneForestTrigger(
    name: String,
    scriptEntity: Entity,
    triggeringEntity: Entity
): TriggerScript = trigger(name, this, triggeringEntity) {
    // TODO start boss cut scene
    actionDelay(0f)
}
