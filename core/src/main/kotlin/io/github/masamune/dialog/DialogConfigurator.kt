package io.github.masamune.dialog

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.component.Name
import ktx.app.gdxError
import ktx.log.logger

class DialogConfigurator(private val bundle: I18NBundle) {

    private val dialogOptionNext = bundle["dialog.option.next"]
    private val dialogOptionOk = bundle["dialog.option.ok"]
    private val dialogOptionExit = bundle["dialog.option.exit"]
    private val dialogOptionBuy = bundle["dialog.option.buy"]

    operator fun get(name: String, world: World, triggeringEntity: Entity): Dialog {
        log.debug { "Creating new dialog $name" }

        return when (name) {
            "elder_00" -> elder00Dialog(name, world, triggeringEntity)
            "elder_10" -> elder10Dialog(name)
            "merchant_00" -> merchantDialog(name)
            "smith_00" -> smithDialog(name, world, triggeringEntity)
            "flower_girl_00" -> flowerGirl00Dialog(name, world, triggeringEntity)
            "flower_girl_10" -> flowerGirl10Dialog(name, world, triggeringEntity)
            "flower_girl_20" -> flowerGirl20Dialog(name, world, triggeringEntity)
            "flower_girl_30" -> flowerGirl30Dialog(name, world, triggeringEntity)
            "villageExit" -> villageExitDialog(name)
            "man_green_00" -> manGreen00Dialog(name)
            "man_green_10" -> manGreen10Dialog(name)

            else -> gdxError("There is no dialog configured for name $name")
        }
    }

    private fun entityName(world: World, entity: Entity): String = with(world) {
        entity[Name].name
    }

    private fun elder00Dialog(name: String, world: World, triggeringEntity: Entity): Dialog = dialog(name) {
        val playerName = entityName(world, triggeringEntity)
        val elderTitle = bundle["npc.elder.title"]

        page(bundle.format("dialog.elder_00.page1", playerName), "elder", elderTitle) {
            option(dialogOptionNext, ActionNext)
        }
        page(bundle.format("dialog.elder_00.page2", playerName), "elder", elderTitle) {
            option(dialogOptionNext, ActionNext)
        }
        page(bundle["dialog.elder_00.page3"], "elder", elderTitle) {
            option(dialogOptionOk, ActionExit)
        }
    }

    private fun elder10Dialog(name: String): Dialog = dialog(name) {
        val elderTitle = bundle["npc.elder.title"]

        page(bundle["dialog.elder_10.page1"], "elder", elderTitle) {
            option(dialogOptionOk, ActionExit)
        }
    }

    private fun merchantDialog(name: String): Dialog = dialog(name) {
        page(bundle["dialog.merchant_00.page1"], "merchant", bundle["npc.merchant.title"]) {
            option(dialogOptionBuy, ActionExit)
            option(dialogOptionExit, ActionExit)
        }
    }

    private fun smithDialog(name: String, world: World, triggeringEntity: Entity): Dialog = dialog(name) {
        val playerName = entityName(world, triggeringEntity)

        page(bundle.format("dialog.smith_00.page1", playerName), "smith", bundle["npc.smith.title"]) {
            option(dialogOptionBuy, ActionExit)
            option(dialogOptionExit, ActionExit)
        }
    }

    private fun flowerGirl00Dialog(name: String, world: World, triggeringEntity: Entity): Dialog = dialog(name) {
        val playerName = entityName(world, triggeringEntity)
        val flowerGirlTitle = bundle["npc.flower_girl.title"]

        page(bundle.format("dialog.flower_girl_00.page1", playerName), "flower_girl", flowerGirlTitle) {
            option(dialogOptionNext, ActionNext)
        }
        page(bundle.format("dialog.flower_girl_00.page2", playerName), "flower_girl", flowerGirlTitle) {
            option(dialogOptionOk, ActionNext)
            option(dialogOptionExit, ActionExit)
        }
        page(bundle.format("dialog.flower_girl_00.page3", playerName), "flower_girl", flowerGirlTitle) {
            option(dialogOptionOk, ActionNext)
        }
        page(bundle.format("dialog.flower_girl_00.page4", playerName), "flower_girl", flowerGirlTitle) {
            option(dialogOptionOk, ActionExit)
        }
    }

    private fun flowerGirl10Dialog(name: String, world: World, triggeringEntity: Entity): Dialog = dialog(name) {
        val playerName = entityName(world, triggeringEntity)

        page(bundle.format("dialog.flower_girl_10.page1", playerName), "flower_girl", bundle["npc.flower_girl.title"]) {
            option(dialogOptionOk, ActionExit)
        }
    }

    private fun flowerGirl20Dialog(name: String, world: World, triggeringEntity: Entity): Dialog = dialog(name) {
        val playerName = entityName(world, triggeringEntity)

        page(bundle.format("dialog.flower_girl_20.page1", playerName), "flower_girl", bundle["npc.flower_girl.title"]) {
            option(dialogOptionOk, ActionExit)
        }
    }

    private fun flowerGirl30Dialog(name: String, world: World, triggeringEntity: Entity): Dialog = dialog(name) {
        val playerName = entityName(world, triggeringEntity)

        page(bundle.format("dialog.flower_girl_30.page1", playerName), "flower_girl", bundle["npc.flower_girl.title"]) {
            option(dialogOptionOk, ActionExit)
        }
    }

    private fun villageExitDialog(name: String): Dialog = dialog(name) {
        page(bundle["dialog.villageExit.page1"]) {
            option(dialogOptionOk, ActionExit)
        }
    }

    private fun manGreen00Dialog(name: String): Dialog = dialog(name) {
        page(bundle["dialog.man_green_00.page1"], "man_green", bundle["npc.man_green.title"]) {
            option(dialogOptionOk, ActionExit)
        }
    }

    private fun manGreen10Dialog(name: String): Dialog = dialog(name) {
        page(bundle["dialog.man_green_10.page1"], "man_green", bundle["npc.man_green.title"]) {
            option(dialogOptionOk, ActionExit)
        }
    }

    companion object {
        private val log = logger<DialogConfigurator>()
    }
}
