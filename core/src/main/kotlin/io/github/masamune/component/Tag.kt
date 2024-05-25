package io.github.masamune.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class Tag : EntityTags by entityTagOf() {
    PLAYER, CAMERA_FOCUS
}
