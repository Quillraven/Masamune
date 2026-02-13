package io.github.masamune.teavm

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.github.tommyettinger.textra.effects.BlinkEffect
import com.github.tommyettinger.textra.effects.EaseEffect
import com.github.tommyettinger.textra.effects.FadeEffect
import com.github.tommyettinger.textra.effects.JumpEffect
import com.github.tommyettinger.textra.effects.RainbowEffect
import com.github.tommyettinger.textra.effects.ShakeEffect
import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend
import com.ray3k.tenpatch.TenPatchDrawable
import io.github.masamune.ui.view.DialogViewStyle
import io.github.masamune.ui.widget.DialogOptionStyle
import org.teavm.vm.TeaVMOptimizationLevel
import java.io.File

/** Builds the TeaVM/HTML application. */
object TeaVMBuilder {
    @JvmStatic
    fun main(arguments: Array<String>) {

        val webBackend = WebBackend()
            .setHtmlTitle("Masamune")
            .setHtmlWidth(960)
            .setHtmlHeight(540)
            .setStartJettyAfterBuild(true)
            .setJettyPort(8080)
            // set 'webAssembly' to false for creating JAVASCRIPT target
            .setWebAssembly(true)

        TeaCompiler(webBackend)
            .addAssets(AssetFileHandle("../assets"))
            .setOptimizationLevel(TeaVMOptimizationLevel.ADVANCED)
            .setMainClass("io.github.masamune.teavm.TeaVMLauncher")
            .setObfuscated(true)
            // Register any classes or packages that require reflection below
            // Scene2D style classes require reflection
            .addReflectionClass(DialogViewStyle::class.java)
            .addReflectionClass(DialogOptionStyle::class.java)
            .addReflectionClass(TenPatchDrawable::class.java)
            // Typing label effects require reflection
            .addReflectionClass(FadeEffect::class.java)
            .addReflectionClass(JumpEffect::class.java)
            .addReflectionClass(RainbowEffect::class.java)
            .addReflectionClass(EaseEffect::class.java)
            .addReflectionClass(BlinkEffect::class.java)
            .addReflectionClass(ShakeEffect::class.java)
            // free type font
            .addReflectionClass(FreeTypeFontGenerator::class.java)
            .build(File("build/dist"))
    }
}
