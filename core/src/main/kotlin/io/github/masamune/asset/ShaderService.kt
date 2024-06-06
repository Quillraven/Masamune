package io.github.masamune.asset

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import ktx.app.gdxError
import ktx.assets.disposeSafely
import ktx.graphics.use
import ktx.math.vec2

class ShaderService(private val fileHandleResolver: FileHandleResolver = InternalFileHandleResolver()) : Disposable {

    private lateinit var outlineShader: ShaderProgram
    private var outlineColorIdx = -1
    private var outlinePixelSizeIdx = -1

    fun loadAllShader() {
        outlineShader = loadShader("default.vert", "outline.frag")
        outlineColorIdx = outlineShader.getUniformLocation("u_outlineColor")
        outlinePixelSizeIdx = outlineShader.getUniformLocation("u_pixelSize")
    }

    private fun loadShader(vertexFile: String, fragmentFile: String): ShaderProgram {
        val vertexCode = fileHandleResolver.resolve("shader/$vertexFile").readString()
        val fragmentCode = fileHandleResolver.resolve("shader/$fragmentFile").readString()
        val shader = ShaderProgram(vertexCode, fragmentCode)
        if (!shader.isCompiled) {
            gdxError("Could not compile shader $vertexFile/$fragmentFile! Log:\n${shader.log}")
        }
        return shader
    }

    fun useOutlineShader(batch: Batch, color: Color, texture: Texture, block: () -> Unit) {
        // set outline shader and its uniforms
        val origShader = batch.shader
        if (origShader != outlineShader) {
            batch.shader = outlineShader
        }
        outlineShader.use {
            it.setUniformf(outlineColorIdx, color)
            it.setUniformf(outlinePixelSizeIdx, TMP_VEC2.set(1f / texture.width, 1f / texture.height))
        }

        // run render block
        block()

        // reset shader to default
        batch.shader = origShader
    }

    override fun dispose() {
        outlineShader.disposeSafely()
    }

    companion object {
        private val TMP_VEC2 = vec2()
    }

}
