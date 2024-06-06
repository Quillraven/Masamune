package io.github.masamune.teavm

import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass
import java.io.File

/** Builds the TeaVM/HTML application. */
@SkipClass
object TeaVMBuilder {
    @JvmStatic
    fun main(arguments: Array<String>) {
        val teaBuildConfiguration = TeaBuildConfiguration().apply {
            assetsPath.add(File("../assets"))
            webappPath = File("build/dist").canonicalPath
            // Register any extra classpath assets here:
            // additionalAssetsClasspathFiles += "io/github/masamune/asset.extension"
            htmlTitle = "Masamune"
            htmlWidth = 960
            htmlHeight = 540
        }

        // Register any classes or packages that require reflection here:
        // TeaReflectionSupplier.addReflectionClass("io.github.masamune.reflect")

        val tool = TeaBuilder.config(teaBuildConfiguration)
        tool.mainClass = "io.github.masamune.teavm.TeaVMLauncher"
        TeaBuilder.build(tool)
    }
}
