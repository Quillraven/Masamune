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
    actionDialog("masamune_forest_00", withSound = false)

    // fire demon
    actionEnableInput(false)
    val spawnDemonFire = tiledService.loadPoint("spawn_demon_1")
    actionDelay(1.5f)
    actionPlaySound(SoundAsset.DEMON_TELEPORT)
    actionSpawnEntity(TiledObjectType.DEMON_FIRE, spawnDemonFire)
    val demonFire = selectEntity { tiledFamily.single { it[Tiled].objType == TiledObjectType.DEMON_FIRE } }
    actionEntitySpeed(demonFire, 2f)
    actionFollowPath(demonFire, 39, removeAtEnd = true, waitForEnd = true)
    actionEnableInput(true)
    actionDialog("masamune_forest_10", withSound = false)

    // spirit demon
    actionEnableInput(false)
    actionDelay(1.5f)
    val spawnDemonSpirit = tiledService.loadPoint("spawn_demon_2")
    actionPlaySound(SoundAsset.DEMON_TELEPORT)
    actionSpawnSfx("portal_white", spawnDemonSpirit - vec2(1.6f, 1.6f), 1f, 0.75f)
    actionDelay(0.3f)
    actionSpawnEntity(TiledObjectType.DEMON_SPIRIT, spawnDemonSpirit)
    val demonSpirit = selectEntity { tiledFamily.single { it[Tiled].objType == TiledObjectType.DEMON_SPIRIT } }
    actionEntitySpeed(demonSpirit, 1.5f)
    actionFollowPath(demonSpirit, 40, removeAtEnd = true, waitForEnd = true)
    actionEnableInput(true)
    actionDialog("masamune_forest_20", withSound = false)

    // cyclops
    actionEnableInput(false)
    actionDelay(1.5f)
    val spawnCyclops = tiledService.loadPoint("spawn_cyclops")
    actionPlaySound(SoundAsset.DEMON_TELEPORT)
    actionSpawnSfx("skull_green", spawnCyclops - vec2(1.6f, 1.6f), 1f, 0.75f)
    actionDelay(0.3f)
    actionSpawnEntity(TiledObjectType.CYCLOPS_NPC, spawnCyclops)
    actionDelay(1.5f)

    // demons removal
    actionEnableInput(true)
    actionDialog("masamune_forest_30", withSound = false)
    actionEnableInput(false)
    actionDelay(1.5f)
    val despawnDemonFire = tiledService.loadPoint("despawn_demon_1")
    actionPlaySound(SoundAsset.DEMON_TELEPORT)
    actionSpawnSfx("portal_red", despawnDemonFire - vec2(1.2f, 1.6f), 1f, 0.75f)
    actionDelay(0.3f)
    actionRemove(demonFire)
    actionDelay(0.5f)
    val despawnDemonSpirit = tiledService.loadPoint("despawn_demon_2")
    actionPlaySound(SoundAsset.DEMON_TELEPORT)
    actionSpawnSfx("portal_white", despawnDemonSpirit - vec2(1.2f, 1.6f), 1f, 0.75f)
    actionDelay(0.3f)
    actionRemove(demonSpirit)

    // TODO start boss combat
    actionEnableInput(true)
}
