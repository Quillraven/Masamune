package io.github.masamune.ui.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import io.github.masamune.audio.AudioService
import io.github.masamune.event.Event

class MainMenuViewModel(
    bundle: I18NBundle,
    audioService: AudioService,
) : ViewModel(bundle, audioService) {

    var musicVolume: Float
        get() = audioService.musicVolume
        set(value) {
            audioService.musicVolume = value
        }

    var soundVolume: Float
        get() = audioService.soundVolume
        set(value) {
            audioService.soundVolume = value
        }

    var startGame = false

    override fun onEvent(event: Event) = Unit

    fun startGame() {
        playSndMenuAccept()
        startGame = true
    }

    fun quitGame() {
        Gdx.app.exit()
    }

}
