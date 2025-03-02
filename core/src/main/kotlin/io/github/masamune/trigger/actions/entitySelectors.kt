package io.github.masamune.trigger.actions

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyDefinition
import com.github.quillraven.fleks.World

sealed interface EntitySelector {
    fun World.selectedEntity(): Entity
}

data class SingleFamilyEntitySelector(
    private val familyCfg: FamilyDefinition.() -> Unit,
) : EntitySelector {
    override fun World.selectedEntity(): Entity = this.family(familyCfg).single()
}
