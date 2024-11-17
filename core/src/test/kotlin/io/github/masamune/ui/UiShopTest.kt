package io.github.masamune.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.component.Inventory
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.event.EventService
import io.github.masamune.event.ShopBeginEvent
import io.github.masamune.gdxTest
import io.github.masamune.tiledmap.TiledStats
import io.github.masamune.ui.model.ShopViewModel
import io.github.masamune.ui.view.shopView
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.toClasspathFile
import ktx.scene2d.actors

fun main() = gdxTest("UI Shop Test", UiShopTest())

private class UiShopTest : KtxApplicationAdapter {
    private val uiViewport = ExtendViewport(928f, 522f)
    private val batch by lazy { SpriteBatch() }
    private val stage by lazy { Stage(uiViewport, batch) }
    private val uiAtlas by lazy { TextureAtlas("ui/skin.atlas".toClasspathFile()) }
    private val skin by lazy { Skin("ui/skin.json".toClasspathFile(), uiAtlas) }
    private val eventService by lazy { EventService() }
    private val world = configureWorld {}
    private val bundle by lazy { I18NBundle.createBundle("ui/messages".toClasspathFile(), Charsets.ISO_8859_1.name()) }
    private val viewModel by lazy { ShopViewModel(bundle, world) }
    private val player by lazy {
        world.entity {
            it += Player()
            it += Inventory(talons = 100)
            it += Stats(
                TiledStats(
                    strength = 10f,
                    agility = 1f,
                    constitution = 2f,
                    intelligence = 5f,
                    damage = 1000f,
                    armor = 5f,
                    resistance = 100f,
                )
            )
        }
    }

    override fun create() {
        stage.actors {
            shopView(viewModel, skin)
        }
        eventService += stage
        eventService.fire(ShopBeginEvent(world, player, Entity.NONE))
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        uiViewport.apply()
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            stage.clear()
            stage.actors {
                shopView(viewModel, skin)
            }
            stage.isDebugAll = false
            eventService.fire(ShopBeginEvent(world, player, Entity.NONE))
        }
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        skin.dispose()
        world.dispose()
    }
}
