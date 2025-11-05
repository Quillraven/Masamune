package io.github.masamune.teavm

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.github.tommyettinger.textra.effects.BlinkEffect
import com.github.tommyettinger.textra.effects.EaseEffect
import com.github.tommyettinger.textra.effects.FadeEffect
import com.github.tommyettinger.textra.effects.JumpEffect
import com.github.tommyettinger.textra.effects.RainbowEffect
import com.github.tommyettinger.textra.effects.ShakeEffect
import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier
import com.ray3k.tenpatch.TenPatchDrawable
import io.github.masamune.ui.view.DialogViewStyle
import io.github.masamune.ui.widget.DialogOptionStyle
import org.teavm.tooling.TeaVMTargetType
import org.teavm.tooling.TeaVMTool
import org.teavm.vm.TeaVMOptimizationLevel
import java.io.File

/** Builds the TeaVM/HTML application. */
object TeaVMBuilder {
    @JvmStatic
    fun main(arguments: Array<String>) {
        val teaBuildConfiguration = TeaBuildConfiguration().apply {
            assetsPath.add(AssetFileHandle("../assets"))
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
        TeaReflectionSupplier.addReflectionClass(TenPatchDrawable::class.java)
        // Typing label effects require reflection
//        TeaReflectionSupplier.addReflectionClass(TypingGlyph::class.java)
        TeaReflectionSupplier.addReflectionClass(FadeEffect::class.java)
        TeaReflectionSupplier.addReflectionClass(JumpEffect::class.java)
        TeaReflectionSupplier.addReflectionClass(RainbowEffect::class.java)
        TeaReflectionSupplier.addReflectionClass(EaseEffect::class.java)
        TeaReflectionSupplier.addReflectionClass(BlinkEffect::class.java)
        TeaReflectionSupplier.addReflectionClass(ShakeEffect::class.java)
        // free type font
        TeaReflectionSupplier.addReflectionClass(FreeTypeFontGenerator::class.java)


        teaBuildConfiguration.targetType = TeaVMTargetType.WEBASSEMBLY_GC
        TeaBuilder.config(teaBuildConfiguration)

        val tool = TeaVMTool()
        tool.mainClass = "io.github.masamune.teavm.TeaVMLauncher"
        tool.optimizationLevel = TeaVMOptimizationLevel.ADVANCED
        // For many (or most) applications, using the highest optimization won't add much to build time.
        // If your builds take too long, and runtime performance doesn't matter, you can change FULL to SIMPLE .
        tool.setObfuscated(true)

        TeaBuilder.build(tool)
    }
}
