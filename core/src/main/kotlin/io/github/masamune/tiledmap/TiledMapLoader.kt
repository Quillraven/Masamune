package io.github.masamune.tiledmap

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.maps.MapProperties
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.utils.XmlReader.Element
import ktx.app.gdxError
import ktx.log.logger

class TiledMapLoader(fileHandleResolver: FileHandleResolver) : TmxMapLoader(fileHandleResolver) {

    private val classFactories = mapOf(
        "Stats" to { classProps: Map<String, Any?> ->
            TiledStats(
                agility = classProps.getOrDefault("agility", 0f) as Float,
                arcaneStrike = classProps.getOrDefault("arcaneStrike", 0f) as Float,
                armor = classProps.getOrDefault("armor", 0f) as Float,
                constitution = classProps.getOrDefault("constitution", 0f) as Float,
                criticalStrike = classProps.getOrDefault("criticalStrike", 0f) as Float,
                damage = classProps.getOrDefault("damage", 0f) as Float,
                intelligence = classProps.getOrDefault("intelligence", 0f) as Float,
                life = classProps.getOrDefault("life", 0f) as Float,
                lifeMax = classProps.getOrDefault("lifeMax", 0f) as Float,
                magicalEvade = classProps.getOrDefault("magicalEvade", 0f) as Float,
                mana = classProps.getOrDefault("mana", 0f) as Float,
                manaMax = classProps.getOrDefault("manaMax", 0f) as Float,
                physicalEvade = classProps.getOrDefault("physicalEvade", 0f) as Float,
                resistance = classProps.getOrDefault("resistance", 0f) as Float,
                strength = classProps.getOrDefault("strength", 0f) as Float,
            )
        }
    )

    override fun loadProperties(mapProperties: MapProperties, xmlElement: Element?) {
        super.loadProperties(mapProperties, xmlElement)

        if (xmlElement?.name == "properties") {
            xmlElement.getChildrenByName("property")
                .filter { it.getAttribute("type", null) == "class" }
                .forEach { loadClassProperty(mapProperties, it) }
        }
    }

    private fun loadClassProperty(mapProperties: MapProperties, xmlElement: Element) {
        val name = xmlElement.getAttribute("name")
        val className = xmlElement.getAttribute("propertytype")
        log.debug { "Loading class property $name with class $className" }

        val classResult = mutableMapOf<String, Any?>()
        xmlElement.getChildrenByName("properties")
            .flatMap { it.getChildrenByName("property") }
            .forEach { classPropXmlElement ->
                val classPropName = classPropXmlElement.getAttribute("name")
                val classPropValue = classPropXmlElement.getAttribute("value")
                val classPropType = classPropXmlElement.getAttribute("type")
                classResult[classPropName] = castProperty(classPropName, classPropValue, classPropType)
            }

        val factory = classFactories[className]
        if (factory != null) {
            mapProperties.put(name, factory(classResult))
        } else {
            mapProperties.put(name, classResult)
        }
    }

    override fun castProperty(name: String?, value: String?, type: String?): Any? {
        when (type) {
            null -> return value
            "int" -> return value?.toInt()
            "float" -> return value?.toFloat()
            "bool" -> return value?.toBoolean()
            "color" -> {
                // Tiled uses the format #AARRGGBB
                val opaqueColor = value?.substring(3)
                val alpha = value?.substring(1, 3)
                return Color.valueOf(opaqueColor + alpha)
            }

            "class" -> return ""

            else -> gdxError("Unsupported type for property $name: $type")
        }
    }

    companion object {
        private val log = logger<TiledMapLoader>()
    }
}
