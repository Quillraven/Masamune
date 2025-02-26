package io.github.masamune.screen

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.Masamune
import io.github.masamune.Masamune.Companion.uiViewport
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.I18NAsset
import io.github.masamune.asset.ShaderService
import io.github.masamune.asset.SkinAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.component.Tag
import io.github.masamune.component.Trigger
import io.github.masamune.dialog.DialogConfigurator
import io.github.masamune.event.EventService
import io.github.masamune.input.KeyboardController
import io.github.masamune.system.AnimationSystem
import io.github.masamune.system.CameraSystem
import io.github.masamune.system.DissolveSystem
import io.github.masamune.system.FacingSystem
import io.github.masamune.system.FadeSystem
import io.github.masamune.system.FollowPathSystem
import io.github.masamune.system.MoveSystem
import io.github.masamune.system.MoveToSystem
import io.github.masamune.system.PhysicSystem
import io.github.masamune.system.RemoveSystem
import io.github.masamune.system.RenderSystem
import io.github.masamune.system.ScaleSystem
import io.github.masamune.system.StateSystem
import io.github.masamune.system.TriggerSystem
import io.github.masamune.tiledmap.TiledService
import io.github.masamune.trigger.TriggerConfigurator
import ktx.app.KtxScreen
import ktx.app.gdxError
import ktx.box2d.createWorld
import ktx.log.logger
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.table

class CutSceneScreen(
    private val masamune: Masamune,
    private val batch: Batch = masamune.batch,
    private val inputProcessor: InputMultiplexer = masamune.inputProcessor,
    private val eventService: EventService = masamune.event,
    private val tiledService: TiledService = masamune.tiled,
    private val shaderService: ShaderService = masamune.shader,
    private val assetService: AssetService = masamune.asset,
    private val audioService: AudioService = masamune.audio,
) : KtxScreen {
    // viewports and stage
    private val gameViewport: Viewport = ExtendViewport(16f, 9f)
    private val uiViewport = uiViewport()
    private val stage = Stage(uiViewport, batch)
    private val skin = assetService[SkinAsset.DEFAULT]

    // physic world
    private val physicWorld = createWorld(Vector2.Zero, true).apply {
        autoClearForces = false
    }

    // other stuff
    private val bundle: I18NBundle = assetService[I18NAsset.MESSAGES]
    private val keyboardController = KeyboardController(eventService)
    private val dialogConfigurator = DialogConfigurator(bundle)
    private val triggerConfigurator = TriggerConfigurator()

    // ecs world
    val world = gameWorld()

    private fun gameWorld() = configureWorld {
        injectables {
            add(batch)
            add(gameViewport)
            add(physicWorld)
            add(shaderService)
            add(eventService)
            add(dialogConfigurator)
            add(triggerConfigurator)
            add(tiledService)
            add(assetService)
            add(masamune)
            add(audioService)
            add(AtlasAsset.SFX.name, assetService[AtlasAsset.SFX])
        }

        systems {
            add(FollowPathSystem())
            add(MoveSystem())
            add(MoveToSystem())
            add(PhysicSystem())
            add(CameraSystem())
            add(StateSystem())
            add(DissolveSystem())
            add(ScaleSystem())
            add(AnimationSystem())
            add(FadeSystem())
            add(RenderSystem())
            add(TriggerSystem())
            // FacingSystem must run at the end of a frame to correctly detect facing changes in any system before
            add(FacingSystem())
            add(RemoveSystem())
        }
    }

    override fun show() {
        // set controller
        inputProcessor.clear()
        inputProcessor.addProcessor(keyboardController)

        // setup UI views
        stage.clear()
        stage.actors {
            table(skin) {
                setFillParent(true)
                label("Cut Scene", "default", skin) {
                    it.grow()
                }
            }
        }

        // register all event listeners
        registerEventListeners()
    }

    fun startCutScene(name: String) {
        when (name) {
            "intro" -> world.entity {
                it += Trigger("cut_scene_intro")
                it += Tag.EXECUTE_TRIGGER
            }

            else -> gdxError("Unsupported cut scene $name")
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
        physicWorld.setContactListener(null)
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        world.update(delta)

        uiViewport.apply()
        stage.act(delta)
        stage.draw()
        batch.setColor(1f, 1f, 1f, 1f)
    }

    override fun dispose() {
        log.debug { "Disposing cut-scene world with '${world.numEntities}' entities" }
        world.dispose()
        physicWorld.dispose()
        stage.dispose()
    }

    companion object {
        private val log = logger<CutSceneScreen>()
    }
}
