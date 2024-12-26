package io.github.masamune.ui.model

import com.badlogic.gdx.utils.I18NBundle
import com.github.quillraven.fleks.World
import io.github.masamune.Masamune
import io.github.masamune.audio.AudioService
import io.github.masamune.component.Experience
import io.github.masamune.component.Tiled
import io.github.masamune.event.CombatPlayerDefeatEvent
import io.github.masamune.event.CombatPlayerVictoryEvent
import io.github.masamune.event.CombatStartEvent
import io.github.masamune.event.Event
import io.github.masamune.event.EventService
import io.github.masamune.event.PlayerInteractCombatEndEvent
import io.github.masamune.screen.BlurTransitionType
import io.github.masamune.screen.CombatScreen
import io.github.masamune.screen.DefaultTransitionType
import io.github.masamune.screen.GameScreen
import ktx.log.logger

enum class UiCombatFinishState {
    UNDEFINED, VICTORY, DEFEAT
}

class CombatFinishViewModel(
    bundle: I18NBundle,
    private val masamune: Masamune,
    private val world: World,
    audioService: AudioService = masamune.audio,
    private val eventService: EventService = masamune.event,
) : ViewModel(bundle, audioService) {

    var xpToGain: Int by propertyNotify(0)
    val combatSummary: MutableMap<String, Int> by propertyNotify(mutableMapOf())
    var state: UiCombatFinishState by propertyNotify(UiCombatFinishState.UNDEFINED)

    override fun onEvent(event: Event) {
        when (event) {
            is CombatStartEvent -> {
                state = UiCombatFinishState.UNDEFINED
                with(world) {
                    var totalXp = 0
                    combatSummary.clear()
                    event.enemies.forEach { enemy ->
                        totalXp += enemy[Experience].current
                        val type = enemy[Tiled].objType
                        val name = bundle["enemy.${type.name.lowercase()}.name"]
                        combatSummary.merge(name, 1, Int::plus)
                    }

                    log.debug { "Total XP to gain: $totalXp" }
                    xpToGain = event.enemies.map { it[Experience].current }.sum()
                    notify(CombatFinishViewModel::combatSummary, combatSummary)
                }
            }

            is CombatPlayerVictoryEvent -> state = UiCombatFinishState.VICTORY

            is CombatPlayerDefeatEvent -> state = UiCombatFinishState.DEFEAT

            else -> Unit
        }
    }

    fun quitCombat() {
        masamune.transitionScreen<GameScreen>(
            fromType = DefaultTransitionType,
            toType = BlurTransitionType(
                startBlur = 6f,
                endBlur = 0f,
                time = 2f,
                endAlpha = 1f,
                startAlpha = 0.4f
            )
        ) {
            val victory = state == UiCombatFinishState.VICTORY
            val gameScreenEntity = masamune.getScreen<CombatScreen>().gameScreenEnemy
            eventService.fire(PlayerInteractCombatEndEvent(victory, gameScreenEntity))
        }
    }

    fun restartCombat() {
        masamune.getScreen<CombatScreen>().restartCombat()
    }

    companion object {
        private val log = logger<CombatFinishViewModel>()
    }

}
