package io.github.masamune

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun gdxTest(title: String, testListener: ApplicationListener) {
    Lwjgl3Application(testListener, Lwjgl3ApplicationConfiguration().apply {
        setTitle(title)
        setWindowedMode(1280, 960)
    })
}
