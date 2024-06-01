package io.github.masamune

import com.badlogic.gdx.graphics.g2d.Batch
import io.github.masamune.asset.AssetService
import io.github.masamune.event.EventService
import io.github.masamune.tiledmap.TiledService
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test

class LazyServiceLocatorTest {

    @Test
    fun `verify TiledService gets correct asset and event service instance`() {
        val batchInitializer = mockk<() -> Batch>()
        val assetServiceInitializer = mockk<() -> AssetService>()
        val eventServiceInitializer = mockk<() -> EventService>()
        val batch = mockk<Batch>()
        val assetService = mockk<AssetService>()
        val eventService = mockk<EventService>()
        every { batchInitializer.invoke() } returns batch
        every { assetServiceInitializer.invoke() } returns assetService
        every { eventServiceInitializer.invoke() } returns eventService

        val serviceLocator = LazyServiceLocator(
            batchInitializer,
            assetServiceInitializer,
            eventServiceInitializer,
            tiledServiceInitializer = { asset, event -> TiledService(asset, event) }
        )

        batch shouldBeSameInstanceAs serviceLocator.batch
        assetService shouldBeSameInstanceAs serviceLocator.asset
        eventService shouldBeSameInstanceAs serviceLocator.event
        serviceLocator.tiled shouldNotBe null
        serviceLocator.tiled.assetService shouldBeSameInstanceAs serviceLocator.asset
        serviceLocator.tiled.eventService shouldBeSameInstanceAs serviceLocator.event
    }

}
