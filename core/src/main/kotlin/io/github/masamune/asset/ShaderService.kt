package io.github.masamune.asset

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.HdpiUtils
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ScreenUtils
import ktx.app.gdxError
import ktx.graphics.use
import ktx.log.logger
import ktx.math.vec2

/**
 * Service class for [ShaderProgram] management. Supports loading and usage of following shaders:
 * - outline
 * - blur
 *
 * If the loading of a shader fails then an exception with the shader's error log is thrown.
 */
class ShaderService(private val fileHandleResolver: FileHandleResolver = InternalFileHandleResolver()) : Disposable {

    /** a temporary [FrameBuffer] that can be used in [renderToFbo] (e.g. for blur shader). */
    private var tmpFbo: FrameBuffer = FrameBuffer(FBO_FORMAT, Gdx.graphics.width, Gdx.graphics.height, false)

    // outline shader
    private lateinit var outlineShader: ShaderProgram
    private var outlineColorIdx = -1
    private var outlinePixelSizeIdx = -1

    // blur shader
    private var blurFbo: FrameBuffer = FrameBuffer(FBO_FORMAT, Gdx.graphics.width, Gdx.graphics.height, false)
    private lateinit var blurShader: ShaderProgram
    private var blurRadiusIdx = -1
    private var blurDirectionIdx = -1

    /**
     * Loads all shaders and stores uniform locations internally for better performance.
     */
    fun loadAllShader() {
        outlineShader = loadShader("default.vert", "outline.frag")
        outlineColorIdx = outlineShader.getUniformLocation("u_outlineColor")
        outlinePixelSizeIdx = outlineShader.getUniformLocation("u_pixelSize")

        blurShader = loadShader("default.vert", "blur.frag")
        blurRadiusIdx = blurShader.getUniformLocation("u_radius")
        blurDirectionIdx = blurShader.getUniformLocation("u_direction")
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
     * Sets the given [shader] as active shader for any draw calls of the [Batch] inside [block].
     * After the draw [block] is executed, the previous shader of the [Batch] is restored.
     */
    private fun Batch.useShader(shader: ShaderProgram, block: () -> Unit) {
        val origShader = this.shader
        if (origShader != shader) {
            this.shader = shader
        }

        block()

        // reset shader to default
        this.shader = origShader
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
        batch.useShader(outlineShader) {
            outlineShader.use {
                it.setUniformf(outlineColorIdx, color)
                it.setUniformf(outlinePixelSizeIdx, TMP_VEC2.set(1f / texture.width, 1f / texture.height))
            }

            // run render block
            block()
        }
    }

    /**
     * Applies a **blur** shader to the given [texture] before rendering it via the [batch].
     * The [radius] defines how much blur is applied. 0 means no blur. Good values are between 0 and 6.
     */
    fun useBlurShader(batch: Batch, radius: Float, texture: Texture) {
        batch.useShader(blurShader) {
            blurShader.use {
                it.setUniformf(blurRadiusIdx, radius)
                it.setUniformf(blurDirectionIdx, Vector2.X)
            }

            // apply horizontal blur to texture by rendering it to FrameBuffer
            blurFbo.renderToFbo {
                // viewport is the entire screen since we render the frame buffer texture pixel perfect 1:1
                // use identity matrix to render pixel perfect
                batch.use(batch.projectionMatrix.idt()) {
                    it.draw(texture, -1f, 1f, 2f, -2f)
                }
            }

            // render to screen by applying vertical blur
            blurShader.use {
                it.setUniformf(blurRadiusIdx, radius)
                it.setUniformf(blurDirectionIdx, Vector2.Y)
            }
            batch.use(batch.projectionMatrix.idt()) {
                it.draw(blurFbo.colorBufferTexture, -1f, 1f, 2f, -2f)
            }
        }
    }

    /**
     * Resize [FrameBuffer] by disposing the current one and recreating a new one with the
     * given new [width] and [height].
     */
    private fun FrameBuffer.resize(width: Int, height: Int): FrameBuffer {
        if (this.width != width || this.height != height) {
            this.dispose()
            return FrameBuffer(FBO_FORMAT, width, height, false)
        }
        return this
    }

    /**
     * Resizes the internal [FrameBuffer] objects.
     */
    fun resize(width: Int, height: Int) {
        log.debug { "Resizing FBOs" }
        blurFbo = blurFbo.resize(width, height)
        tmpFbo = tmpFbo.resize(width, height)
    }

    /**
     * Disposes all shaders.
     */
    override fun dispose() {
        outlineShader.dispose()
        blurFbo.dispose()
        tmpFbo.dispose()
    }

    companion object {
        private val TMP_VEC2 = vec2()

        // FBO format requires ALPHA support for proper overlapping rendering of fbo textures like
        // in the blur screen transition
        private val FBO_FORMAT = Format.RGBA8888
        private val log = logger<ShaderService>()

        /**
         * Renders anything inside [block] to the [FrameBuffer]
         * and returns its [colorBufferTexture][FrameBuffer.getColorBufferTexture].
         */
        fun FrameBuffer.renderToFbo(block: () -> Unit): Texture {
            this.use {
                // !! IMPORTANT !!
                // use a clear color with alpha 0f instead of 1f because otherwise,
                // its transparent pixels will overlap any pixels of previous render calls due
                // to missing transparency.
                ScreenUtils.clear(0f, 0f, 0f, 0f, false)
                HdpiUtils.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
                block()
            }
            return this.colorBufferTexture
        }
    }

}
