package io.github.masamune.asset

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import ktx.app.gdxError
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.graphics.use

class ShaderService : Disposable {

    private lateinit var outlineShader: ShaderProgram
    private var outlineColorIdx = -1

    fun loadAllShader() {
        outlineShader = loadShader("default.vert", "outline.frag")
        outlineColorIdx = outlineShader.getUniformLocation("u_outlineColor")
    }

    private fun loadShader(vertexFile: String, fragmentFile: String): ShaderProgram {
        val vertexCode = "shader/$vertexFile".toInternalFile().readString()
        val fragmentCode = "shader/$fragmentFile".toInternalFile().readString()
        val shader = ShaderProgram(vertexCode, fragmentCode)
        if (!shader.isCompiled) {
            gdxError("Could not compile shader $vertexFile/$fragmentFile! Log:\n${shader.log}")
        }
        return shader
    }

    fun useOutlineShader(batch: Batch, color: Color, block: () -> Unit) {
        // set outline shader and its uniforms
        if (batch.shader != outlineShader) {
            batch.shader = outlineShader
        }
        outlineShader.use { it.setUniformf(outlineColorIdx, color) }

        // run render block
        block()

        // reset shader to default
        batch.shader = null
    }

    override fun dispose() {
        outlineShader.disposeSafely()
    }

}
