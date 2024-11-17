package io.github.masamune.ui.model

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.World
import io.github.masamune.component.Inventory
import io.github.masamune.component.Stats
import io.github.masamune.event.Event
import io.github.masamune.event.ShopBeginEvent

class ShopViewModel(
    private val bundle: I18NBundle,
    private val world: World,
) : ViewModel() {

    // pair-left = localized text for UIStat, pair-right = value
    var playerStats: Map<UIStats, Pair<String, String>> by propertyNotify(emptyMap())

    override fun onEvent(event: Event) {
        if (event !is ShopBeginEvent) {
            return
        }

        with(event.world) {
            val player = event.player
            playerStats = player[Stats].toUiMap(bundle) and player[Inventory].toUiMap(bundle)
        }
    }

}
