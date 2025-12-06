package io.github.masamune.trigger.cutscene.outro

import com.badlogic.gdx.utils.Align
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.masamune.asset.MusicAsset
import io.github.masamune.screen.FadeTransitionType
import io.github.masamune.screen.MainMenuScreen
import io.github.masamune.trigger.TriggerScript
import io.github.masamune.trigger.trigger
import io.github.masamune.ui.model.I18NKey

// not used anymore for now because Outro of demo was replaced with a real elder dialog so
// that people can continue playing and spend the money of the boss fight
//
// might be useful for future real outro/credit scenes, so let's keep it
fun World.cutSceneOutroTrigger(
    name: String,
    scriptEntity: Entity,
): TriggerScript = trigger(name, this, Entity.NONE) {
    actionRemove(scriptEntity)
    actionPlayMusic(MusicAsset.FOREST)
    actionDelay(1f)
    actionCutSceneText(I18NKey.CUT_SCENE_OUTRO_TEXT1, Align.center, duration = 9f)
    actionCutSceneText(I18NKey.CUT_SCENE_OUTRO_TEXT2, Align.center, duration = 7f)
    actionCutSceneText(I18NKey.CUT_SCENE_OUTRO_TEXT3, Align.center, duration = 7f)
    actionChangeScreen {
        transitionScreen<MainMenuScreen>(
            FadeTransitionType(1f, 0f, 2f),
            FadeTransitionType(0f, 1f, 0.25f, delayInSeconds = 2f),
        )
    }
}
