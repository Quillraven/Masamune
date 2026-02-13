@file:JvmName("TeaVMLauncher")

package io.github.masamune.teavm

import com.github.xpenatan.gdx.teavm.backends.web.WebApplication
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration
import com.github.xpenatan.gdx.teavm.backends.web.WebAssetPreloadListener
import io.github.masamune.Masamune

/** Launches the TeaVM/HTML application. */
fun main() {
    val config = WebApplicationConfiguration("canvas").apply {
        //// If width and height are each greater than 0, then the app will use a fixed size.
        //config.width = 640;
        //config.height = 480;
        //// If width and height are both 0, then the app will use all available space.
        //config.width = 0;
        //config.height = 0;
        //// If width and height are both -1, then the app will fill the canvas size.
        width = 0
        height = 0
        showDownloadLogs = true
        preloadListener = WebAssetPreloadListener { assetLoader -> assetLoader.loadScript("freetype.js") }
    }
    WebApplication(Masamune(webLauncher = true), config)
}
