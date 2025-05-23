package io.github.masamune.util

import com.badlogic.gdx.utils.XmlReader
import io.github.masamune.util.tiled.Member
import io.github.masamune.util.tiled.TiledProject
import kotlinx.serialization.json.Json
import java.io.File

private const val AUTO_GEN_INFO_TEXT =
    "This is an autogenerated class by gradle's 'genTiledEnumsAndExtensions' task. Do not touch it!"

fun main() {
    val file = File("../assets/maps/masamune-tiled.tiled-project")

    println("Reading file ${file.name}")
    val jsonStr = file.readText()

    println("Parsing Json content")
    val json = Json {
        ignoreUnknownKeys = true
    }
    val tiledProject = json.decodeFromString<TiledProject>(jsonStr)

    parseEnums(tiledProject)
    parseExtensions(tiledProject)
}

private fun parseExtensions(
    tiledProject: TiledProject,
) {
    println("Generating property extensions")
    val extensionContent = createPropertyExtensionsHeader()
    // name of tiled classes to process
    listOf(
        "FixtureDefinition" to "MapObject",
        "Portal" to "MapObject",
        "ItemObject" to "TiledMapTile",
        "EnemyObject" to "TiledMapTile",
        "NpcObject" to "TiledMapTile",
        "PlayerObject" to "TiledMapTile",
        "PropObject" to "TiledMapTile",
    ).forEach { (tiledClass, gdxClass) ->
        val properties = tiledProject.propertyTypes
            .first { it.name == tiledClass && it.members.isNotEmpty() }
            .members
        createPropertyExtensions(extensionContent, properties, gdxClass)
    }
    createPropertyExtensionsFile(extensionContent)
}

private fun parseEnums(tiledProject: TiledProject) {
    println("Generating enums")
    // map of 'tiled enum name' to 'kotlin class name'
    // if map does not contain the tiled name then nothing will be generated
    val supportedEnums = mapOf(
        "MapObjectType" to "TiledObjectType",
        "ItemType" to "ItemType",
        "AnimationType" to "AnimationType",
        "ItemCategory" to "ItemCategory",
        "ActionType" to "ActionType",
        "ConsumableType" to "ConsumableType",
    )
    supportedEnums.forEach { (tiledEnum, masamuneEnum) ->
        tiledProject.propertyTypes
            .filter { it.name == tiledEnum && it.values.isNotEmpty() }
            .forEach { createEnum(masamuneEnum, it.values) }
    }
}

fun createEnum(enumName: String, values: List<String>) {
    println("Creating enum $enumName with values $values")
    val enumTargetPackage = "io/github/masamune/tiledmap"
    val enumFile = File("../core/src/main/kotlin/$enumTargetPackage/$enumName.kt")
    if (enumFile.exists()) {
        enumFile.delete()
    }
    enumFile.createNewFile()

    val content = buildString {
        appendLine("package io.github.masamune.tiledmap")
        appendLine()

        if ("ActionType" == enumName) {
            this.createActionTypeEnum(enumName, values)
            return@buildString
        } else if ("TiledObjectType" == enumName) {
            this.createMapObjectTypeEnum(enumName, values)
            return@buildString
        }

        appendLine("// $AUTO_GEN_INFO_TEXT")
        appendLine("enum class $enumName {")
        appendLine(
            values.sorted().joinToString(separator = ",${System.lineSeparator()}    ", prefix = "    ", postfix = ";")
        )

        when (enumName) {
            "AnimationType" -> {
                appendLine()
                appendLine("    val atlasKey: String = this.name.lowercase()")
            }

            "ItemCategory" -> {
                appendLine()
                appendLine("    val isEquipment: Boolean")
                appendLine("        get() = this == ACCESSORY || this == ARMOR || this == BOOTS || this == HELMET || this == WEAPON")
            }
        }

        appendLine("}")
    }

    enumFile.writeText(content)
}

private fun StringBuilder.createMapObjectTypeEnum(enumName: String, values: List<String>) {
    appendLine("// $AUTO_GEN_INFO_TEXT")
    appendLine("enum class $enumName(val isEnemy: Boolean) {")

    values.forEach { value ->
        appendLine("    $value(${isEnemyObjectType(value)}),")
    }

    appendLine("}")
}

private val objectTiles by lazy {
    val file = File("../assets/maps/objects.tsx")
    val element = XmlReader().parse(file.reader())
    element.getChildrenByName("tile")
}

private fun isEnemyObjectType(value: String): Boolean {
    return objectTiles
        .filter { it.attributes["type"] == "EnemyObject" }
        .any { tile ->
            val tileProperties = tile.getChildByName("properties")
            val objTypeProperty = tileProperties.children.singleOrNull { it.attributes["name"] == "objType" }
            val objType = objTypeProperty?.attributes?.get("value")
            return@any objType != null && objType == value
        }
}


private fun StringBuilder.createActionTypeEnum(enumName: String, values: List<String>) {
    fun String.toCamelCase(): String {
        return this.lowercase().replace("_[a-z]".toRegex()) { it.value.last().uppercase() }
    }

    appendLine("import io.github.masamune.combat.action.*")
    appendLine()
    appendLine("// $AUTO_GEN_INFO_TEXT")
    appendLine("enum class $enumName(private val actionFactory: () -> Action) {")
    values.sorted().forEach { value ->
        if ("UNDEFINED".equals(value, ignoreCase = true)) {
            appendLine("    $value({ DefaultAction }),")
        } else {
            val actionName = value.toCamelCase()
            appendLine("    $value(::${actionName.first().uppercase() + actionName.substring(1)}Action),")
        }
    }
    appendLine("    ;")
    appendLine()
    appendLine("    operator fun invoke() = actionFactory()")
    appendLine("}")
}

fun createPropertyExtensionsHeader(): StringBuilder {
    return StringBuilder().apply {
        val newLine = System.lineSeparator()
        append("package io.github.masamune.tiledmap").append(newLine).append(newLine)
        append("import com.badlogic.gdx.maps.MapObject").append(newLine)
        append("import com.badlogic.gdx.maps.MapProperties").append(newLine)
        append("import com.badlogic.gdx.maps.tiled.TiledMapTile").append(newLine)
        append("import ktx.tiled.property").append(newLine).append(newLine)
        append("import ktx.tiled.propertyOrNull").append(newLine).append(newLine)
        append("// This is an autogenerated file by gradle's 'genTiledEnumsAndExtensions' task. Do not touch it!")
    }
}

fun createPropertyExtensionsFile(content: StringBuilder) {
    val targetPackage = "io/github/masamune/tiledmap"
    val targetFile = File("../core/src/main/kotlin/$targetPackage/tiledProperties.kt")
    if (targetFile.exists()) {
        targetFile.delete()
    }
    targetFile.createNewFile()
    content.append(System.lineSeparator())
    targetFile.writeText(content.toString())
}

private val alreadyProcessedProperties = mutableSetOf<String>()

fun createPropertyExtensions(
    content: StringBuilder,
    properties: List<Member>,
    gdxClass: String,
) {
    println("Creating property extensions for ${properties.map(Member::name)} and class $gdxClass")

    with(content) {
        val newLine = System.lineSeparator()

        properties.forEach { property ->
            if (!alreadyProcessedProperties.add(property.name)) {
                return@forEach
            }

            append(newLine).append(newLine)
            if (property.name == "userData") {
                // special case with String? and propertyOrNull ktx call
                append("val $gdxClass.${property.name}: ${property.kotlinType}?").append(newLine)
                append("    get() = this.propertyOrNull<${property.kotlinType}>(\"${property.name}\")")
                return@forEach
            } else if ("class" == property.type) {
                // special case for class types
                append("val $gdxClass.${property.name}: MapProperties?").append(newLine)
                append("    get() = this.propertyOrNull<MapProperties>(\"${property.name}\")")
                return@forEach
            }

            append("val $gdxClass.${property.name}: ${property.kotlinType}").append(newLine)
            append("    get() = this.property<${property.kotlinType}>(\"${property.name}\", ${property.kotlinValue})")
        }
    }
}

fun String.toMasamuneClassname() = when (this) {
    "stats" -> "Stats"
    else -> error("Unsupported Tiled class name: $this")
}
