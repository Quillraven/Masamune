package io.github.masamune.ui.model

import com.badlogic.gdx.utils.Align

data class CutSceneTextModel(
    val text: String,
    val align: Int,
) {
    companion object {
        val EMPTY = CutSceneTextModel("", Align.center)
    }
}
