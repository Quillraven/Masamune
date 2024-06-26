package io.github.masamune.dialog

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Player
import ktx.app.gdxError
import ktx.log.logger

class DialogConfigurator(private val bundle: I18NBundle) {

    private val dialogOptionNext = bundle["dialog.option.next"]
    private val dialogOptionOk = bundle["dialog.option.ok"]

    operator fun get(name: String, world: World, triggeringEntity: Entity): Dialog {
        log.debug { "Creating new dialog $name" }

        return when (name) {
            "elder_00" -> elderDialog(name, world, triggeringEntity)

            else -> gdxError("There is no dialog configured for name $name")
        }
    }

    private fun entityName(world: World, entity: Entity): String = with(world) {
        entity[Player].name
    }

    private fun elderDialog(name: String, world: World, triggeringEntity: Entity): Dialog = dialog(name) {
        val playerName = entityName(world, triggeringEntity)
        val elderTitle = bundle["npc.elder.title"]

        page(bundle.format("dialog.elder_00.page1", playerName), "elder", elderTitle) {
            option(dialogOptionNext, ActionNext)
        }
        page(bundle["dialog.elder_00.page2"], "elder", elderTitle) {
            option(dialogOptionOk, ActionExit)
        }
    }

    companion object {
        private val log = logger<DialogConfigurator>()
    }
}
