package io.github.masamune.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

private const val AUTO_GEN_INFO_TEXT =
    "This is an autogenerated class by gradle's 'genTiledEnumsAndExtensions' task. Do not touch it!"

@Serializable
data class Member(
    val name: String,
    val type: String,
    val value: JsonElement,
    val propertyType: String = "",
) {
    val kotlinType: String = when (type) {
        "string", "color" -> "String"
        "bool" -> "Boolean"
        "float" -> "Float"
        "int" -> "Int"
        "class" -> name.toMasamuneClassname()
        else -> error("Unsupported type: $type")
    }

    val kotlinValue: String = when (type) {
        "string", "color" -> "\"${(value as JsonPrimitive).content}\""
        "float" -> "${(value as JsonPrimitive).content}f"
        "class" -> ""
        else -> (value as JsonPrimitive).content
    }
}

@Serializable
data class PropertyType(
    val id: Int,
    val name: String,
    val type: String,
    val values: List<String> = emptyList(),
    val members: List<Member> = emptyList(),
)

@Serializable
data class TiledProject(
    val propertyTypes: List<PropertyType>,
)

fun main() {
    val file = File("../masamune-tiled.tiled-project")

    println("Reading file ${file.name}")
    val jsonStr = file.readText()

    println("Parsing Json content")
    val json = Json {
        ignoreUnknownKeys = true
    }
    val tiledProject = json.decodeFromString<TiledProject>(jsonStr)

    println("Generating enums")
    // map of 'tiled enum name' to 'kotlin class name'
    // if map does not contain the tiled name then nothing will be generated
    val supportedEnums = mapOf(
        "MapObjectType" to "TiledObjectType",
        "ItemType" to "ItemType",
        "AnimationType" to "AnimationType",
    )
    supportedEnums.forEach { (tiledEnum, masamuneEnum) ->
        tiledProject.propertyTypes
            .filter { it.name == tiledEnum && it.values.isNotEmpty() }
            .forEach { createEnum(masamuneEnum, it.values) }
    }

    println("Generating classes")
    // name of tiled classes for which Masamune Kotlin classes should be created (Tiled name to Masamune Kotlin name)
    val tiledClassToMasamuneClass = listOf(
        "Stats" to "TiledStats",
    )
    tiledClassToMasamuneClass.forEach { (tiledClass, masamuneClass) ->
        val members = tiledProject.propertyTypes
            .first { it.name == tiledClass && it.members.isNotEmpty() }
            .members
        createClass(masamuneClass, members)
    }

    println("Generating property extensions")
    val extensionContent = createPropertyExtensionsHeader()
    // name of tiled classes to process
    listOf(
        "MapObject" to "TiledMapTile",
        "FixtureDefinition" to "MapObject",
        "Portal" to "MapObject",
        "ItemObject" to "TiledMapTile",
    ).forEach { (tiledClass, gdxClass) ->
        val properties = tiledProject.propertyTypes
            .first { it.name == tiledClass && it.members.isNotEmpty() }
            .members
        createPropertyExtensions(extensionContent, properties, gdxClass, tiledClassToMasamuneClass)
    }
    createPropertyExtensionsFile(extensionContent)
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
        val newLine = System.lineSeparator()
        append("package io.github.masamune.tiledmap").append(newLine).append(newLine)
        append("// $AUTO_GEN_INFO_TEXT").append(newLine)
        append("enum class $enumName {").append(newLine)
        append(values.sorted().joinToString(separator = ",$newLine    ", prefix = "    ", postfix = ";"))
        append(newLine)

        if (enumName == "AnimationType") {
            append(newLine).append("    val atlasKey: String = this.name.lowercase()").append(newLine)
        }

        append("}").append(newLine)
    }

    enumFile.writeText(content)
}

fun createClass(className: String, members: List<Member>) {
    println("Creating class $className with ${members.size} members")
    val classTargetPackage = "io/github/masamune/tiledmap"
    val classFile = File("../core/src/main/kotlin/$classTargetPackage/$className.kt")
    if (classFile.exists()) {
        classFile.delete()
    }
    classFile.createNewFile()

    val content = buildString {
        val newLine = System.lineSeparator()
        append("package io.github.masamune.tiledmap").append(newLine).append(newLine)
        append("// $AUTO_GEN_INFO_TEXT").append(newLine)
        append("data class $className(").append(newLine)
        append(
            members.joinToString(
                separator = ",$newLine    ",
                prefix = "    ",
                transform = { member -> "var ${member.name}: ${member.kotlinType} = ${member.kotlinValue}" },
                postfix = newLine
            )
        )
        append(")")

        if (className == "TiledStats") {
            append(" {").append(newLine)
            append("    fun isAllNull(): Boolean {").append(newLine)
            append("        return ")
            append(
                members.joinToString(
                    separator = "$newLine            && ",
                    prefix = "",
                    transform = { member -> "${member.name} == 0f" },
                    postfix = newLine
                )
            )
            append("    }").append(newLine)
            append("}").append(newLine)
        } else {
            append(newLine)
        }
    }

    classFile.writeText(content)
}

fun createPropertyExtensionsHeader(): StringBuilder {
    return StringBuilder().apply {
        val newLine = System.lineSeparator()
        append("package io.github.masamune.tiledmap").append(newLine).append(newLine)
        append("import com.badlogic.gdx.maps.MapObject").append(newLine)
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
    tiledClassToMasamuneClass: List<Pair<String, String>>
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
                val masamuneClass = tiledClassToMasamuneClass.first { it.first == property.kotlinType }.second
                append("val $gdxClass.${property.name}: $masamuneClass?").append(newLine)
                append("    get() = this.propertyOrNull<$masamuneClass>(\"${property.name}\")")
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
