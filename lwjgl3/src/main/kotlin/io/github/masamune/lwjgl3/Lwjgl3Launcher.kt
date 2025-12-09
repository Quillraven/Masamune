@file:JvmName("Lwjgl3Launcher")

package io.github.masamune.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import io.github.masamune.Masamune

/** Launches the desktop (LWJGL3) application. */
fun main() {
    // This handles macOS support and helps on Windows.
    if (StartupHelper.startNewJvmIfRequired())
        return
    Lwjgl3Application(Masamune(webLauncher = false), Lwjgl3ApplicationConfiguration().apply {
        setTitle("Masamune")
        setWindowIcon("icon32.png")
        val displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode()
        setDecorated(false)
        setWindowedMode(displayMode.width, displayMode.height)
    })
}
