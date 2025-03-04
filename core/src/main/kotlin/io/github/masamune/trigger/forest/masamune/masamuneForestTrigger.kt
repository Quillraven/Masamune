package io.github.masamune.trigger.forest.masamune

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.asset.SoundAsset
import io.github.masamune.component.Tiled
import io.github.masamune.tiledmap.TiledObjectType
import io.github.masamune.tiledmap.TiledService
import io.github.masamune.trigger.TriggerScript
import io.github.masamune.trigger.trigger
import ktx.math.minus
import ktx.math.vec2

fun World.masamuneForestTrigger(
    name: String,
    triggeringEntity: Entity
): TriggerScript = trigger(name, this, triggeringEntity) {
    val tiledFamily = family { all(Tiled) }
    val tiledService = inject<TiledService>()

    actionFadeOutMusic(2f, wait = false)
    actionDialog("masamune_forest_00")

    // fire demon part 1
    val spawnDemonFire = tiledService.loadPoint("spawn_demon_1")
    actionEnableInput(false)
    actionDelay(1.5f)
    actionPlaySound(SoundAsset.DEMON_TELEPORT)
    actionSpawnEntity(TiledObjectType.DEMON_FIRE, spawnDemonFire)
    val demonFire = selectEntity { tiledFamily.single { it[Tiled].objType == TiledObjectType.DEMON_FIRE } }
    actionEntitySpeed(demonFire, 2f)
    actionFollowPath(demonFire, 39, removeAtEnd = true, waitForEnd = true)

    // spirit demon part 1
    val spawnDemonSpirit = tiledService.loadPoint("spawn_demon_2")
    actionPlaySound(SoundAsset.DEMON_TELEPORT)
    actionSpawnSfx("portal_white", spawnDemonSpirit - vec2(1.6f, 1.6f), 1f, 0.75f)
    actionDelay(0.3f)
    actionSpawnEntity(TiledObjectType.DEMON_SPIRIT, spawnDemonSpirit)
    val demonSpirit = selectEntity { tiledFamily.single { it[Tiled].objType == TiledObjectType.DEMON_SPIRIT } }
    actionEntitySpeed(demonSpirit, 1.5f)
    actionFollowPath(demonSpirit, 40, removeAtEnd = true, waitForEnd = true)

    actionEnableInput(true)
}
