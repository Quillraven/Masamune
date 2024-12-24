package io.github.masamune.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.Masamune
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.asset.I18NAsset
import io.github.masamune.asset.ShaderService
import io.github.masamune.asset.ShaderService.Companion.useShader
import io.github.masamune.asset.SkinAsset
import io.github.masamune.asset.SoundAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.combat.ActionExecutorService
import io.github.masamune.component.Animation
import io.github.masamune.component.Combat
import io.github.masamune.component.Facing
import io.github.masamune.component.FacingDirection
import io.github.masamune.component.Graphic
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.ScreenBgd
import io.github.masamune.component.Stats
import io.github.masamune.component.Transform
import io.github.masamune.event.CombatStartEvent
import io.github.masamune.event.EventService
import io.github.masamune.input.ControllerStateUI
import io.github.masamune.input.KeyboardController
import io.github.masamune.system.AnimationSystem
import io.github.masamune.system.CombatSystem
import io.github.masamune.system.DissolveSystem
import io.github.masamune.system.RenderSystem
import io.github.masamune.system.ScaleSystem
import io.github.masamune.system.SelectorSystem
import io.github.masamune.system.ShakeSystem
import io.github.masamune.tiledmap.ActionType
import io.github.masamune.tiledmap.AnimationType
import io.github.masamune.ui.model.CombatViewModel
import io.github.masamune.ui.view.combatView
import ktx.app.KtxScreen
import ktx.log.logger
import ktx.math.vec2
import ktx.math.vec3
import ktx.scene2d.actors

class CombatScreen(
    private val masamune: Masamune,
    private val batch: Batch = masamune.batch,
    private val inputProcessor: InputMultiplexer = masamune.inputProcessor,
    private val eventService: EventService = masamune.event,
    private val shaderService: ShaderService = masamune.shader,
    private val assetService: AssetService = masamune.asset,
    private val audioService: AudioService = masamune.audio,
    private val actionExecutorService: ActionExecutorService = masamune.actionExecutor,
) : KtxScreen {
    // viewports and stage
    private val gameViewport: Viewport = ExtendViewport(8f, 8f)
    private val uiViewport = ExtendViewport(928f, 522f)
    private val stage = Stage(uiViewport, batch)
    private val skin = assetService[SkinAsset.DEFAULT]

    // other stuff
    private val bundle: I18NBundle = assetService[I18NAsset.MESSAGES]
    private val keyboardController = KeyboardController(eventService, initialState = ControllerStateUI::class)

    // ecs world
    private val world = combatWorld()
    private val enemyEntities = world.family { none(Player).all(Combat) }

    // view model
    private val combatViewModel = CombatViewModel(
        bundle,
        audioService,
        world,
        eventService,
        gameViewport,
        uiViewport,
        assetService[AtlasAsset.CHARS_AND_PROPS],
        actionExecutorService
    )

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
                add(actionExecutorService)
            }

            systems {
                add(CombatSystem())
                add(AnimationSystem())
                add(DissolveSystem())
                add(ScaleSystem())
                add(ShakeSystem())
                add(SelectorSystem())
                add(ScreenBgdRenderSystem())
                add(RenderSystem())
            }
        }
    }

    override fun show() {
        // set action executor entity world
        actionExecutorService.world = world

        // set controller
        inputProcessor.clear()
        inputProcessor.addProcessor(keyboardController)

        // setup UI views
        stage.clear()
        stage.actors {
            combatView(combatViewModel, skin)
        }

        // register all event listeners
        registerEventListeners()

        // special screen background entity to render GameScreen as blurred background
        world.entity {
            it += ScreenBgd(alpha = 0.4f) { batch, fbo ->
                shaderService.useBlurShader(batch, 6f, fbo) {
                    val gameScreen = masamune.getScreen<GameScreen>()
                    gameScreen.resize(fbo.width, fbo.height)
                    gameScreen.render(0f)
                }
            }
        }

        spawnDummyCombatEntity(agility = 3f, damage = 2f, offsetXY = vec2(-3f, -2f))
        spawnDummyCombatEntity(agility = 7f, damage = 3f, offsetXY = vec2(-1f, -4f))
        spawnDummyCombatEntity(agility = 1f, damage = 1f, offsetXY = vec2(1f, -3f))
    }

    fun spawnPlayer(gameScreenWorld: World, gameScreenPlayer: Entity) {
        val atlas = assetService[AtlasAsset.CHARS_AND_PROPS]
        val nameCmp = with(gameScreenWorld) { gameScreenPlayer[Name] }
        val playerCmp = with(gameScreenWorld) { gameScreenPlayer[Player] }
        val statsCmp = with(gameScreenWorld) { gameScreenPlayer[Stats] }
        val combatCmp = with(gameScreenWorld) { gameScreenPlayer[Combat] }

        val combatPlayer = this.world.entity {
            it += nameCmp
            it += playerCmp
            it += Stats.of(statsCmp)
            it += Facing(FacingDirection.UP)
            val animationCmp = Animation.ofAtlas(atlas, "hero", AnimationType.WALK, FacingDirection.UP, speed = 0.4f)
            it += animationCmp
            val graphicCmp = Graphic(animationCmp.gdxAnimation.getKeyFrame(0f))
            it += graphicCmp
            it += Transform(vec3(gameViewport.worldWidth * 0.5f + 1f, 1f, 0f), graphicCmp.regionSize)
            it += Combat(
                availableActionTypes = combatCmp.availableActionTypes.toMutableList(),
                attackSnd = SoundAsset.SWORD_SWIPE
            )
        }

        eventService.fire(CombatStartEvent(combatPlayer, enemyEntities.entities))
    }

    private fun spawnDummyCombatEntity(agility: Float, damage: Float, offsetXY: Vector2) {
        val atlas = assetService[AtlasAsset.CHARS_AND_PROPS]

        world.entity {
            it += Name("Dummy1")
            it += Stats(strength = 2f, agility = agility, damage = damage, armor = 5f, life = 20f, lifeMax = 20f)
            it += Facing(FacingDirection.DOWN)
            val animationCmp =
                Animation.ofAtlas(atlas, "butterfly", AnimationType.WALK, FacingDirection.DOWN, speed = 0.4f)
            it += animationCmp
            val graphicCmp = Graphic(animationCmp.gdxAnimation.getKeyFrame(0f))
            it += graphicCmp
            it += Transform(
                vec3(gameViewport.worldWidth * 0.5f + offsetXY.x, gameViewport.worldHeight + offsetXY.y, 0f),
                graphicCmp.regionSize
            )
            it += Combat(availableActionTypes = listOf(ActionType.ATTACK_SINGLE))
        }
    }

    private fun registerEventListeners() {
        eventService += world
        eventService += stage
        eventService += keyboardController
        eventService += masamune.audio
    }

    override fun hide() {
        // in case of defeat we set a grayscale shader -> remove that shader if it was set
        batch.shader = null
        eventService.clearListeners()
        world.removeAll(clearRecycled = true)
        audioService.playPrevMusic()
        combatViewModel.reset()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiViewport.update(width, height, true)
        world.system<ScreenBgdRenderSystem>().onResize(width, height)
        combatViewModel.onResize()
    }

    override fun render(delta: Float) {
        world.update(delta)

        batch.useShader(null) {
            uiViewport.apply()
            stage.act(delta)
            stage.draw()
        }

        // TODO remove debug
        when {
            Gdx.input.isKeyJustPressed(Input.Keys.X) -> {
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

            Gdx.input.isKeyJustPressed(Input.Keys.Z) -> {
                world.family { all(ScreenBgd) }.forEach { it.remove() }
            }
        }
    }

    override fun dispose() {
        log.debug { "Disposing world with '${world.numEntities}' entities" }
        world.dispose()
        stage.dispose()
    }

    companion object {
        private val log = logger<CombatScreen>()
    }
}
