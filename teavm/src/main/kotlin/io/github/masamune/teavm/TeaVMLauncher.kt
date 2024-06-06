@file:JvmName("TeaVMLauncher")

package io.github.masamune.teavm

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration
import com.github.xpenatan.gdx.backends.teavm.TeaApplication
import io.github.masamune.Masamune

/** Launches the TeaVM/HTML application. */
fun main() {
    val config = TeaApplicationConfiguration("canvas").apply {
        width = 0
        height = 0
    }
    TeaApplication(Masamune(), config)
}
