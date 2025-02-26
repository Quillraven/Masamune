package io.github.masamune.trigger

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.Masamune
import io.github.masamune.asset.MusicAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.dialog.DialogConfigurator
import io.github.masamune.event.EventService
import io.github.masamune.quest.Quest
import io.github.masamune.tiledmap.ItemType
import io.github.masamune.tiledmap.TiledService
import io.github.masamune.trigger.TriggerActionDialog.Companion.NO_CLOSE_ACTION
import io.github.masamune.ui.model.I18NKey
import ktx.app.gdxError


@DslMarker
annotation class TriggerDsl

@TriggerDsl
class TriggerCfg(
    val world: World,
    val triggeringEntity: Entity,
    private val actions: MutableList<TriggerAction>,
) {
    fun actionRemove(entity: Entity) {
        actions += TriggerActionRemoveEntity(entity)
    }

    fun actionDialog(dialogName: String, closeAction: (selectedOptionIdx: Int) -> Unit = NO_CLOSE_ACTION) {
        val configurator = world.inject<DialogConfigurator>()
        val eventService = world.inject<EventService>()
        actions += TriggerActionDialog(configurator, dialogName, world, triggeringEntity, eventService, closeAction)
    }

    fun actionAddItem(entity: Entity, itemType: ItemType) {
        val tiledService = world.inject<TiledService>()
        val eventService = world.inject<EventService>()
        val audioService = world.inject<AudioService>()
        actions += TriggerActionAddItem(entity, itemType, eventService, tiledService, audioService)
    }

    fun actionRemoveItem(entity: Entity, itemType: ItemType, amount: Int) {
        actions += TriggerActionRemoveItem(entity, itemType, amount)
    }

    fun actionAddQuest(entity: Entity, quest: Quest) {
        val eventService = world.inject<EventService>()
        actions += TriggerActionAddQuest(entity, quest, eventService)
    }

    fun actionCompleteQuest(quest: Quest) {
        val eventService = world.inject<EventService>()
        actions += TriggerActionCompleteQuest(quest, eventService)
    }

    fun actionMoveBack(entity: Entity, distance: Float, timeInSeconds: Float = 0f, wait: Boolean = true) {
        actions += TriggerActionMoveBack(entity, distance, timeInSeconds, wait)
    }

    fun actionShop(playerEntity: Entity, shopEntity: Entity, shopName: I18NKey, items: List<ItemType>) {
        val tiledService = world.inject<TiledService>()
        actions += TriggerActionShop(playerEntity, shopEntity, shopName, items, tiledService)
    }

    fun actionHeal(entity: Entity, healLife: Boolean, healMana: Boolean) {
        val audioService = world.inject<AudioService>()
        actions += TriggerActionHeal(entity, healLife, healMana, audioService)
    }

    fun actionPauseEntity(entity: Entity, pause: Boolean) {
        actions += TriggerActionPauseEntity(entity, pause)
    }

    fun actionPlayMusic(music: MusicAsset, loop: Boolean = true, keepPrevious: Boolean = false) {
        val audioService = world.inject<AudioService>()
        actions += TriggerActionPlayMusic(audioService, music, loop, keepPrevious)
    }

    fun actionDelay(seconds: Float) {
        actions += TriggerActionDelay(seconds)
    }

    fun actionChangeScreen(action: Masamune.() -> Unit) {
        val masamune = world.inject<Masamune>()
        actions += TriggerActionChangeScreen(masamune, action)
    }
}

fun trigger(
    name: String,
    world: World,
    triggeringEntity: Entity,
    cfg: TriggerCfg.() -> Unit
): TriggerScript {
    val actions = mutableListOf<TriggerAction>()
    TriggerCfg(world, triggeringEntity, actions).apply(cfg)
    if (actions.isEmpty()) {
        gdxError("Trigger $name must have at least one action")
    }

    return TriggerScript(name, world, actions)
}
