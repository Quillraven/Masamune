package io.github.masamune.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.HdpiUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.Masamune
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.I18NAsset
import io.github.masamune.asset.ShaderService
import io.github.masamune.asset.ShaderService.Companion.resize
import io.github.masamune.asset.SkinAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.AttackAction
import io.github.masamune.component.Animation
import io.github.masamune.component.Combat
import io.github.masamune.component.Facing
import io.github.masamune.component.FacingDirection
import io.github.masamune.component.Graphic
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.component.Transform
import io.github.masamune.event.CombatPlayerActionEvent
import io.github.masamune.event.CombatStartEvent
import io.github.masamune.event.EventService
import io.github.masamune.input.KeyboardController
import io.github.masamune.system.AnimationSystem
import io.github.masamune.system.CombatSystem
import io.github.masamune.system.RenderSystem
import io.github.masamune.tiledmap.AnimationType
import ktx.app.KtxScreen
import ktx.graphics.component1
import ktx.graphics.component2
import ktx.graphics.component3
import ktx.graphics.component4
import ktx.graphics.use
import ktx.log.logger
import ktx.math.vec3

class CombatScreen(
    private val masamune: Masamune,
    private val batch: Batch = masamune.batch,
    private val inputProcessor: InputMultiplexer = masamune.inputProcessor,
    private val eventService: EventService = masamune.event,
    private val shaderService: ShaderService = masamune.shader,
    private val assetService: AssetService = masamune.asset,
    private val audioService: AudioService = masamune.audio,
) : KtxScreen {
    // viewports and stage
    private val gameViewport: Viewport = ExtendViewport(8f, 8f)
    private val uiViewport = ExtendViewport(928f, 522f)
    private val stage = Stage(uiViewport, batch)
    private val skin = assetService[SkinAsset.DEFAULT]

    // other stuff
    private val bundle: I18NBundle = assetService[I18NAsset.MESSAGES]
    private val keyboardController = KeyboardController(eventService)
    private var fbo = FrameBuffer(ShaderService.FBO_FORMAT, Gdx.graphics.width, Gdx.graphics.height, false)

    // ecs world
    private val world = combatWorld()
    private val playerEntities = world.family { all(Player, Combat) }
    private val enemyEntities = world.family { none(Player).all(Combat) }

    private fun combatWorld(): World {
        return configureWorld {
            injectables {
                add(batch)
                add(gameViewport)
                add(shaderService)
                add(eventService)
                add(assetService)
                add(masamune)
                add(audioService)
            }

            systems {
                add(CombatSystem())
                add(AnimationSystem())
                add(RenderSystem())
            }
        }
    }

    override fun show() {
        // set controller
        inputProcessor.clear()
        inputProcessor.addProcessor(keyboardController)

        // register all event listeners
        registerEventListeners()

        updateBgdFbo(Gdx.graphics.width, Gdx.graphics.height)

        spawnDummyCombatEntities()
        eventService.fire(CombatStartEvent)
    }

    private fun spawnDummyCombatEntities() {
        val atlas = assetService[AtlasAsset.CHARS_AND_PROPS]

        // player
        world.entity {
            it += Name("Player")
            it += Player()
            it += Stats(strength = 10f, agility = 5f, damage = 1f, armor = 0f, life = 10f, lifeMax = 10f)
            it += Facing(FacingDirection.UP)
            val animationCmp = Animation.ofAtlas(atlas, "hero", AnimationType.WALK, FacingDirection.UP, speed = 0.4f)
            it += animationCmp
            val graphicCmp = Graphic(animationCmp.gdxAnimation.getKeyFrame(0f))
            it += graphicCmp
            it += Transform(vec3(gameViewport.worldWidth * 0.5f + 1f, 1f, 0f), graphicCmp.regionSize)
            it += Combat()
        }

        // enemy
        world.entity {
            it += Name("Dummy")
            it += Stats(strength = 2f, agility = 3f, damage = 3f, armor = 5f, life = 20f, lifeMax = 20f)
            it += Facing(FacingDirection.DOWN)
            val animationCmp =
                Animation.ofAtlas(atlas, "butterfly", AnimationType.WALK, FacingDirection.DOWN, speed = 0.4f)
            it += animationCmp
            val graphicCmp = Graphic(animationCmp.gdxAnimation.getKeyFrame(0f))
            it += graphicCmp
            it += Transform(
                vec3(gameViewport.worldWidth * 0.5f - 1f, gameViewport.worldHeight - 2f, 0f),
                graphicCmp.regionSize
            )
            it += Combat()
        }
    }

    private fun updateBgdFbo(width: Int, height: Int) {
        shaderService.useBlurShader(batch, 6f, fbo) {
            val gameScreen = masamune.getScreen<GameScreen>()
            gameScreen.resize(width, height)
            gameScreen.render(0f)
        }
    }

    private fun registerEventListeners() {
        eventService += world
        eventService += stage
        eventService += keyboardController
        eventService += masamune.audio
    }

    override fun hide() {
        eventService.clearListeners()
        world.removeAll(clearRecycled = true)
        audioService.playPrevMusic()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiViewport.update(width, height, true)
        fbo = fbo.resize(width, height)
        updateBgdFbo(width, height)
    }

    override fun render(delta: Float) {
        // render blurred out GameScreen as background
        HdpiUtils.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        batch.use(batch.projectionMatrix.idt()) {
            val (r, g, b, a) = batch.color
            batch.setColor(r, g, b, 0.4f)
            it.draw(fbo.colorBufferTexture, -1f, 1f, 2f, -2f)
            batch.setColor(r, g, b, a)
        }
        world.update(delta)

        uiViewport.apply()
        stage.act(delta)
        stage.draw()

        // TODO remove debug
        when {
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> with(world) {
                val player = playerEntities.first()
                player[Combat].action = AttackAction(player).also { action ->
                    action.targets += enemyEntities.first()
                }
                eventService.fire(CombatPlayerActionEvent(player))
            }

            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) -> {
                masamune.transitionScreen<GameScreen>(
                    fromType = DefaultTransitionType,
                    toType = BlurTransitionType(
                        startBlur = 6f,
                        endBlur = 0f,
                        time = 2f,
                        endAlpha = 1f,
                        startAlpha = 0.4f
                    )
                )
            }
        }
    }

    override fun dispose() {
        log.debug { "Disposing world with '${world.numEntities}' entities" }
        world.dispose()
        stage.dispose()
        fbo.dispose()
    }

    companion object {
        private val log = logger<CombatScreen>()
    }
}
