package io.github.masamune.teavm

import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass
import com.rafaskoberg.gdx.typinglabel.TypingGlyph
import com.rafaskoberg.gdx.typinglabel.effects.FadeEffect
import io.github.masamune.ui.view.DialogViewStyle
import io.github.masamune.ui.widget.DialogOptionStyle
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
        // Scene2D style classes require reflection
        TeaReflectionSupplier.addReflectionClass(DialogViewStyle::class.java)
        TeaReflectionSupplier.addReflectionClass(DialogOptionStyle::class.java)
        // Typing label effects require reflection
        TeaReflectionSupplier.addReflectionClass(TypingGlyph::class.java)
        TeaReflectionSupplier.addReflectionClass(FadeEffect::class.java)

        val tool = TeaBuilder.config(teaBuildConfiguration)
        tool.mainClass = "io.github.masamune.teavm.TeaVMLauncher"
        TeaBuilder.build(tool)
    }
}
