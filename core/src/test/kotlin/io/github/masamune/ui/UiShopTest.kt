package io.github.masamune.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.MutableEntityBag
import com.github.quillraven.fleks.configureWorld
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.AtlasAsset
import io.github.masamune.audio.AudioService
import io.github.masamune.component.Graphic
import io.github.masamune.component.Inventory
import io.github.masamune.component.Item
import io.github.masamune.component.Name
import io.github.masamune.component.Player
import io.github.masamune.component.Stats
import io.github.masamune.event.EventService
import io.github.masamune.event.ShopBeginEvent
import io.github.masamune.gdxTest
import io.github.masamune.input.ControllerStateUI
import io.github.masamune.input.KeyboardController
import io.github.masamune.tiledmap.ItemCategory
import io.github.masamune.tiledmap.ItemType
import io.github.masamune.tiledmap.TiledService
import io.github.masamune.tiledmap.TiledStats
import io.github.masamune.ui.model.ShopViewModel
import io.github.masamune.ui.view.shopView
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.app.gdxError
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
    private val assetService by lazy { AssetService(ClasspathFileHandleResolver()) }
    private val tiledService by lazy { mockk<TiledService>() }
    private val world = configureWorld {}
    private val bundle by lazy { I18NBundle.createBundle("ui/messages".toClasspathFile(), Charsets.ISO_8859_1.name()) }
    private val viewModel by lazy { ShopViewModel(bundle, world, tiledService, eventService) }
    private val audioService by lazy { AudioService(assetService) }
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
    private val shop by lazy {
        world.entity {
            it += Name("Merchant")
            it += Inventory(items = MutableEntityBag(16).apply {
                this += createItem(world, ItemType.ELDER_SWORD)
                this += createItem(world, ItemType.STUDDED_LEATHER)
                this += createItem(world, ItemType.HELMET)
                this += createItem(world, ItemType.BOOTS)
                this += createItem(world, ItemType.RING)
                this += createItem(world, ItemType.SMALL_MANA_POTION)
            })
        }
    }

    private fun createItem(world: World, type: ItemType): Entity = world.entity {
        val atlas = assetService[AtlasAsset.CHARS_AND_PROPS]

        it += Name(type.name.lowercase())
        it += Graphic(atlas.findRegions("items/${type.name.lowercase()}").first())
        when (type) {
            ItemType.ELDER_SWORD -> {
                it += Stats(TiledStats(damage = 3f, intelligence = 1f))
                it += Item(type, 50, ItemCategory.WEAPON, "item.${type.name.lowercase()}.description")
            }

            ItemType.STUDDED_LEATHER -> {
                it += Stats(TiledStats(armor = 5f))
                it += Item(type, 150, ItemCategory.ARMOR, "item.${type.name.lowercase()}.description")
            }

            ItemType.HELMET -> {
                it += Stats(TiledStats(armor = 2f))
                it += Item(type, 40, ItemCategory.HELMET, "item.${type.name.lowercase()}.description")
            }

            ItemType.BOOTS -> {
                it += Stats(TiledStats(armor = 1f))
                it += Item(type, 30, ItemCategory.BOOTS, "item.${type.name.lowercase()}.description")
            }

            ItemType.RING -> {
                it += Stats(TiledStats(strength = 1f, agility = 1f))
                it += Item(type, 100, ItemCategory.ACCESSORY, "item.${type.name.lowercase()}.description")
            }

            ItemType.SMALL_MANA_POTION -> {
                it += Stats(TiledStats(mana = 15f))
                it += Item(type, 10, ItemCategory.OTHER, "item.${type.name.lowercase()}.description")
            }

            else -> gdxError("Unsupported item type: $type")
        }
    }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        val itemTypeSlot = slot<ItemType>()
        every {
            tiledService.loadItem(eq(world), capture(itemTypeSlot))
        } answers {
            createItem(world, itemTypeSlot.captured)
        }

        stage.actors {
            shopView(viewModel, skin)
        }
        eventService += stage
        eventService += audioService
        Gdx.input.inputProcessor = KeyboardController(eventService, ControllerStateUI::class).also {
            eventService += it
        }

        assetService.load(AtlasAsset.CHARS_AND_PROPS)
        assetService.finishLoading()

        eventService.fire(ShopBeginEvent(world, player, shop))
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
            eventService += stage
            eventService.fire(ShopBeginEvent(world, player, shop))
        }
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        skin.dispose()
        world.dispose()
        assetService.dispose()
    }
}
