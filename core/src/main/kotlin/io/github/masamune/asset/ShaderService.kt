package io.github.masamune.asset

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import ktx.app.gdxError
import ktx.graphics.use
import ktx.math.vec2

/**
 * Service class for [ShaderProgram] management. Supports loading and usage of following shaders:
 * - outline
 *
 * If the loading of a shader fails then an exception with the shader's error log is thrown.
 */
class ShaderService(private val fileHandleResolver: FileHandleResolver = InternalFileHandleResolver()) : Disposable {

    private lateinit var outlineShader: ShaderProgram
    private var outlineColorIdx = -1
    private var outlinePixelSizeIdx = -1

    /**
     * Loads all shaders and stores uniform locations internally for better performance.
     */
    fun loadAllShader() {
        outlineShader = loadShader("default.vert", "outline.frag")
        outlineColorIdx = outlineShader.getUniformLocation("u_outlineColor")
        outlinePixelSizeIdx = outlineShader.getUniformLocation("u_pixelSize")
    }

    /**
     * Loads a specific shader via its [vertexFile] and [fragmentFile].
     * If loading fails then an exception with the shader's error log is thrown.
     */
    private fun loadShader(vertexFile: String, fragmentFile: String): ShaderProgram {
        val vertexCode = fileHandleResolver.resolve("shader/$vertexFile").readString()
        val fragmentCode = fileHandleResolver.resolve("shader/$fragmentFile").readString()
        val shader = ShaderProgram(vertexCode, fragmentCode)
        if (!shader.isCompiled) {
            gdxError("Could not compile shader $vertexFile/$fragmentFile! Log:\n${shader.log}")
        }
        return shader
    }

    /**
     * Sets an **outline** shader as active shader for any draw calls of the [batch].
     * The line has the given [color].
     * The [texture] for the draw calls must be provided to correctly calculate the size of a pixel
     * inside the shader program.
     * After the draw [block] is executed, the previous shader of the [batch] is restored.
     */
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

    /**
     * Disposes all shaders.
     */
    override fun dispose() {
        outlineShader.dispose()
    }

    companion object {
        private val TMP_VEC2 = vec2()
    }

}
