package io.github.masamune.audio

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import io.github.masamune.asset.AssetService
import io.github.masamune.asset.MusicAsset
import io.github.masamune.asset.SoundAsset
import io.github.masamune.event.DialogBackEvent
import io.github.masamune.event.DialogOptionChangeEvent
import io.github.masamune.event.DialogOptionTriggerEvent
import io.github.masamune.event.Event
import io.github.masamune.event.EventListener
import io.github.masamune.event.MapChangeEvent
import io.github.masamune.event.MenuBeginEvent
import io.github.masamune.ui.model.MenuType
import ktx.log.logger
import ktx.tiled.propertyOrNull

class AudioService(
    val assetService: AssetService,
) : EventListener {

    private var prevMusic: MusicAsset? = null
    private var lastMusic: Pair<Music, MusicAsset>? = null
    private val soundCache = mutableMapOf<SoundAsset, Sound>()

    var musicVolume: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            lastMusic?.first?.volume = field
        }

    var soundVolume: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    fun play(musicAsset: MusicAsset, loop: Boolean = true) {
        // stop previous music instance if there is any and unload it
        lastMusic?.let { (prevMusic, prevMusicAsset) ->
            log.debug { "Unloading previous music $prevMusicAsset" }
            prevMusic.stop()
            assetService.unload(prevMusicAsset)
        }
        prevMusic = lastMusic?.second

        // load new music asset and finish unloading/loading process
        assetService.load(musicAsset)
        assetService.finishLoading()

        // play new music
        log.debug { "Playing new music $musicAsset" }
        with(assetService[musicAsset]) {
            isLooping = loop
            volume = musicVolume
            play()
            // remember music asset and music instance for unloading/stopping later on
            lastMusic = Pair(this, musicAsset)
        }
    }

    fun playPrevMusic() {
        prevMusic?.let { play(it) }
    }

    fun play(soundAsset: SoundAsset, pitch: Float = 1f) {
        if (soundCache.size > 100) {
            // cache is to big -> unload current sound instances
            log.info { "Sound cache exceeds size of 100 -> clearing it" }
            soundCache.keys.forEach(assetService::unload)
            assetService.finishLoading()
            soundCache.clear()
        }

        if (soundAsset !in soundCache) {
            // load sound instance into sound cache
            log.debug { "Adding $soundAsset to sound cache" }
            assetService.load(soundAsset)
            assetService.finishLoading()
            soundCache[soundAsset] = assetService[soundAsset]
            log.debug { "${soundCache.size} sound entries in sound cache" }
        }

        soundCache[soundAsset]?.play(soundVolume, pitch.coerceIn(0.5f, 2f), 0f)
    }

    fun pause() {
        soundCache.values.forEach { it.pause() }
        lastMusic?.first?.pause()
    }

    fun resume() {
        soundCache.values.forEach { it.resume() }
        lastMusic?.first?.play()
    }

    override fun onEvent(event: Event) {
        when {
            event is DialogOptionChangeEvent -> play(SoundAsset.MENU_CLICK)
            event is DialogOptionTriggerEvent -> play(SoundAsset.MENU_ACCEPT)
            event is DialogBackEvent -> play(SoundAsset.MENU_ABORT)
            event is MenuBeginEvent && event.type == MenuType.GAME -> play(SoundAsset.MENU_ACCEPT)
            event is MapChangeEvent -> {
                event.tiledMap.propertyOrNull<String>("music")?.let { musicAssetStr ->
                    val musicAsset = MusicAsset.valueOf(musicAssetStr)
                    play(musicAsset, true)
                }
            }

            else -> Unit
        }
    }

    override fun toString(): String {
        return "AudioService(musicVolume=$musicVolume, soundVolume=$soundVolume, lastMusic=$lastMusic)"
    }

    companion object {
        private val log = logger<AudioService>()
    }

}
