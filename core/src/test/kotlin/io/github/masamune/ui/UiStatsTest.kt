package io.github.masamune.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.component.Experience
import io.github.masamune.component.Inventory
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.event.EventService
import io.github.masamune.event.MenuBeginEvent
import io.github.masamune.gdxTest
import io.github.masamune.ui.model.MenuType
import io.github.masamune.ui.model.StatsViewModel
import io.github.masamune.ui.view.statsView
import io.mockk.mockk
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.toClasspathFile
import ktx.scene2d.actors

fun main() = gdxTest("UI Stats Test; 1-4 change HP,MP,XP values", UiStatsTest())

private class UiStatsTest : KtxApplicationAdapter {
    private val uiViewport = ExtendViewport(928f, 522f)
    private val batch by lazy { SpriteBatch() }
    private val stage by lazy { Stage(uiViewport, batch) }
    private val uiAtlas by lazy { TextureAtlas("ui/skin.atlas".toClasspathFile()) }
    private val skin by lazy { Skin("ui/skin.json".toClasspathFile(), uiAtlas) }
    private val eventService by lazy { EventService() }
    private val world = configureWorld {}
    private val bundle by lazy { I18NBundle.createBundle("ui/messages".toClasspathFile(), Charsets.ISO_8859_1.name()) }
    private val viewModel by lazy { StatsViewModel(bundle, mockk(), world, eventService) }

    override fun create() {
        stage.actors {
            statsView(viewModel, skin)
        }
        eventService += stage
        world.entity {
            it += Player()
            it += Inventory(talons = 100)
            it += Name("Test Hero")
            it += Experience(level = 2)
            it += Stats(
                strength = 10f,
                lifeMax = 100f,
                life = 100f,
                manaMax = 30f,
                mana = 30f,
                criticalStrike = 0.15f,
                arcaneStrike = -0.25f,
                physicalEvade = 1f,
                magicalEvade = 1.25f,
            )
        }

        eventService.fire(MenuBeginEvent(MenuType.STATS))
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    private fun updatePlayer(lifePerc: Float, manaPerc: Float, xpPerc: Float) = with(world) {
        val playerEntity = family { all(Player) }.first()
        val stats = playerEntity[Stats]
        stats.life = stats.lifeMax * lifePerc
        stats.mana = stats.manaMax * manaPerc
        val experience = playerEntity[Experience]
        experience.current = (experience.forLevelUp * xpPerc).toInt()

        eventService.fire(MenuBeginEvent(MenuType.STATS))
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        uiViewport.apply()
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()

        when {
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> updatePlayer(lifePerc = 0f, manaPerc = 0f, xpPerc = 0f)
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) -> updatePlayer(
                lifePerc = 0.25f,
                manaPerc = 0.25f,
                xpPerc = 0.25f
            )

            Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) -> updatePlayer(
                lifePerc = 0.75f,
                manaPerc = 0.75f,
                xpPerc = 0.75f
            )

            Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) -> updatePlayer(lifePerc = 1f, manaPerc = 1f, xpPerc = 1f)
        }
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        skin.dispose()
        world.dispose()
    }
}
