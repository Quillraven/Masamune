package io.github.masamune.dialog

import ktx.app.gdxError
import ktx.log.Logger

private val dialogCache = mutableListOf<Dialog>()
private val log = Logger("DialogCfg")

fun dialogOf(name: String): Dialog {
    val cachedDialog = dialogCache.firstOrNull { it.name == name }
    if (cachedDialog != null) {
        return cachedDialog
    }

    log.debug { "Creating new dialog $name" }
    if (dialogCache.size >= 4) {
        log.debug { "Removing one dialog of the cache" }
        dialogCache.removeFirst()
    }

    val newDialog: Dialog = dialogByName(name)

    dialogCache += newDialog
    return newDialog
}

private fun dialogByName(name: String) = when (name) {
    "elder_00" -> dialog(name) {
        page("Hello hero", "elder", "Elder") {
            option("OK", ActionExit)
            option("Quit", ActionExit)
        }
    }

    else -> gdxError("There is no dialog configured for name $name")
}
