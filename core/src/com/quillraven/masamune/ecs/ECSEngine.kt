package com.quillraven.masamune.ecs

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.SerializationException
import com.quillraven.masamune.MainGame
import com.quillraven.masamune.ecs.component.RemoveComponent
import com.quillraven.masamune.ecs.system.*

private const val TAG = "ECSEngine"

class ECSEngine constructor(private val game: MainGame) : PooledEngine(), Disposable {

    init {
        addSystem(IdentifySystem(game))
        addSystem(DescriptionSystem(game))
        addSystem(PlayerInputSystem(game))
        addSystem(ActionableSystem(game))
        addSystem(InventorySystem(game))
        addSystem(EquipmentSystem(game))
        addSystem(AttributeSystem(game))
        addSystem(ExperienceSystem(game))
        addSystem(ConversationSystem(game))
        addSystem(HealSystem(game))
        addSystem(RenderFlipSystem(game))
        addSystem(Box2DSystem(game))
        addSystem(CameraSystem(game)) // add AFTER box2d system to use the calculated interpolated values
        addSystem(GameRenderSystem(game))
        addSystem(RemoveSystem())

        // debug stuff
        addSystem(Box2DDebugRenderSystem(game))
    }

    override fun dispose() {
        for (system in systems) {
            if (system is Disposable) {
                system.dispose()
            }
        }
    }

    private fun getComponentClassByName(cmpName: String): Class<Component>? {
        val cmpClass = try {
            Class.forName(cmpName)
        } catch (e: Exception) {
            Gdx.app.error(TAG, "There is no component class of type $cmpName", e)
            return null
        }
        try {
            @Suppress("UNCHECKED_CAST")
            return cmpClass as Class<Component>
        } catch (e: ClassCastException) {
            Gdx.app.error(TAG, "Cannot cast class to component class", e)
            return null
        }
    }

    // cmpData is a json array of serialized component data
    fun createEntityFromConfig(cmpData: JsonValue, posX: Float = 0f, posY: Float = 0f, widthScale: Float = 1f, heightScale: Float = 1f) {
        val entity = createEntity()

        var iterator: JsonValue? = cmpData
        while (iterator != null) {
            val value = iterator
            iterator = iterator.next

            val cmpClass = getComponentClassByName(value.getString("class")) ?: continue
            val cmp = createComponent(cmpClass)
            entity.add(cmp)
            try {
                game.json.readFields(cmp, value)
            } catch (e: SerializationException) {
                Gdx.app.error(TAG, "Cannot set fields of component $cmp", e)
            }
        }

        // set special location for entity if specified
        val transformCmp = game.cmpMapper.transform.get(entity)
        if (transformCmp != null && (posX != 0f || posY != 0f)) {
            transformCmp.x = posX
            transformCmp.y = posY
        }

        // initialize width and height of transform component with default values if needed
        val renderCmp = game.cmpMapper.render.get(entity)
        if (renderCmp != null && transformCmp != null && transformCmp.width == 0f && transformCmp.height == 0f) {
            transformCmp.width = renderCmp.width * widthScale
            transformCmp.height = renderCmp.height * heightScale
        }

        addEntity(entity)
    }

    fun destroyNonPlayerEntitiesOfType(type: EntityType) {
        val playerEntity = getSystem(IdentifySystem::class.java).getPlayerEntity()
        val playerInventory = game.cmpMapper.inventory.get(playerEntity)

        for (entity in getSystem(IdentifySystem::class.java).getEntitiesOfType(type)) {
            if (entity == playerEntity) continue
            if (playerInventory != null) {
                var found = false
                for (idx in 0 until playerInventory.items.size) {
                    if (playerInventory.items[idx] != DEFAULT_ENTITY_ID && game.cmpMapper.identify.get(entity).id == playerInventory.items[idx]) {
                        found = true
                        break
                    }
                }
                if (found) continue
            }

            entity.add(createComponent(RemoveComponent::class.java))
        }
    }
}
