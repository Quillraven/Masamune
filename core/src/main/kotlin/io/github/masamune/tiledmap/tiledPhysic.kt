package io.github.masamune.tiledmap

import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.github.quillraven.fleks.World
import io.github.masamune.Masamune.Companion.UNIT_SCALE
import io.github.masamune.PhysicWorld
import ktx.app.gdxError
import ktx.box2d.FixtureDefinition
import ktx.box2d.body
import ktx.box2d.box
import ktx.collections.GdxArray
import ktx.math.*
import ktx.tiled.*

fun TiledMap.spawnBoundaryBodies(world: World) {
    // create four boxes for the map boundary (left, right, bottom and top edge)
    val physicWorld = world.inject<PhysicWorld>()
    val mapW = width.toFloat()
    val mapH = height.toFloat()
    val halfW = width * 0.5f
    val halfH = height * 0.5f
    val boxThickness = 1f

    physicWorld.body(BodyType.StaticBody) {
        position.set(0f, 0f)

        // left edge
        box(boxThickness, mapH, vec2(-boxThickness * 0.5f, halfH))
        // right edge
        box(boxThickness, mapH, vec2(mapW + boxThickness * 0.5f, halfH))

        // bottom edge
        box(mapW, boxThickness, vec2(halfW, -boxThickness * 0.5f))
        // top edge
        box(mapW, boxThickness, vec2(halfW, mapH + boxThickness * 0.5f))
    }
}

fun TiledMapTile.toBody(
    world: World,
    x: Float,
    y: Float,
    bodyType: BodyType,
    data: Any? = null
): Body {
    if (objects.isEmpty()) {
        gdxError("Trying to create a physic body without fixtures for tile ${this.id}")
    }

    val physicWorld = world.inject<PhysicWorld>()
    return physicWorld.body(bodyType) {
        position.set(x, y)
        fixedRotation = true
        userData = data
        fixtureDefinitions.addAll(toFixtureDefs())
    }
}

fun TiledMapTile.toFixtureDefs(): GdxArray<FixtureDefinition> {
    val result = GdxArray<FixtureDefinition>()
    objects.forEach { mapObject ->
        result.add(
            when (mapObject) {
                is RectangleMapObject -> rectangleFixtureDef(mapObject)
                is EllipseMapObject -> ellipseFixtureDef(mapObject)
                is PolygonMapObject -> polygonFixtureDef(mapObject)
                is PolylineMapObject -> polylineFixtureDef(mapObject)
                else -> gdxError("Unsupported MapObject $mapObject")
            }
        )
    }
    return result
}

// box is centered around body position in Box2D, but we want to have it aligned in a way
// that the body position is the bottom left corner of the box.
// That's why we use a 'boxOffset' below.
private fun rectangleFixtureDef(mapObject: RectangleMapObject): FixtureDefinition {
    val (rectX, rectY, rectW, rectH) = mapObject.rectangle
    val boxX = rectX * UNIT_SCALE
    val boxY = rectY * UNIT_SCALE

    val boxW = rectW * UNIT_SCALE * 0.5f
    val boxH = rectH * UNIT_SCALE * 0.5f
    return FixtureDefinition().apply {
        shape = PolygonShape().apply {
            setAsBox(boxW, boxH, vec2(boxX + boxW, boxY + boxH), 0f)
        }
    }
}

private fun ellipseFixtureDef(mapObject: EllipseMapObject): FixtureDefinition {
    val (x, y, w, h) = mapObject.ellipse
    val ellipseX = x * UNIT_SCALE
    val ellipseY = y * UNIT_SCALE
    val ellipseW = w * UNIT_SCALE / 2f
    val ellipseH = h * UNIT_SCALE / 2f

    return if (MathUtils.isEqual(ellipseW, ellipseH, 0.1f)) {
        // width and height are equal -> return a circle shape
        FixtureDefinition().apply {
            shape = CircleShape().apply {
                position = vec2(ellipseX + ellipseW, ellipseY + ellipseH)
                radius = ellipseW
            }
        }
    } else {
        // width and height are not equal -> return an ellipse shape (=polygon with 'numVertices' vertices)
        // PolygonShape only supports 8 vertices
        // ChainShape supports more but does not properly collide in some scenarios
        val numVertices = 8
        val angleStep = MathUtils.PI2 / numVertices
        val vertices = Array(numVertices) { vertexIdx ->
            val angle = vertexIdx * angleStep
            val offsetX = ellipseW * MathUtils.cos(angle)
            val offsetY = ellipseH * MathUtils.sin(angle)
            vec2(ellipseX + ellipseW + offsetX, ellipseY + ellipseH + offsetY)
        }

        FixtureDefinition().apply {
            shape = PolygonShape().apply {
                set(vertices)
            }
        }
    }
}

private fun polylineFixtureDef(mapObject: PolylineMapObject): FixtureDefinition {
    return polygonFixtureDef(mapObject.x, mapObject.y, mapObject.polyline.vertices, false)
}

private fun polygonFixtureDef(mapObject: PolygonMapObject): FixtureDefinition {
    return polygonFixtureDef(mapObject.x, mapObject.y, mapObject.polygon.vertices, true)
}

private fun polygonFixtureDef(
    polyX: Float,
    polyY: Float,
    polyVertices: FloatArray,
    loop: Boolean,
): FixtureDefinition {
    val x = polyX * UNIT_SCALE
    val y = polyY * UNIT_SCALE
    val vertices = FloatArray(polyVertices.size) { vertexIdx ->
        if (vertexIdx % 2 == 0) {
            x + polyVertices[vertexIdx] * UNIT_SCALE
        } else {
            y + polyVertices[vertexIdx] * UNIT_SCALE
        }
    }

    return FixtureDefinition().apply {
        shape = ChainShape().apply {
            if (loop) {
                createLoop(vertices)
            } else {
                createChain(vertices)
            }
        }
    }
}
