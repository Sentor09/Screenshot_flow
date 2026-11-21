package com.flowsnap.canvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CanvasDomainTest {
    @Test fun viewportRoundTripsBetweenCanvasAndScreenCoordinates() {
        val viewport = CanvasViewport(zoom = 2f, pan = CanvasPoint(40f, -10f))
        val canvasPoint = CanvasPoint(12f, 30f)

        assertEquals(canvasPoint, viewport.toCanvas(viewport.toScreen(canvasPoint)))
    }

    @Test fun zoomingAroundFocalPointKeepsThatPointStationary() {
        val viewport = CanvasViewport(zoom = 1.25f, pan = CanvasPoint(10f, 20f))
        val focalPoint = CanvasPoint(300f, 240f)

        val zoomed = viewport.zoomedAround(focalPoint, targetZoom = 2.5f)

        assertEquals(focalPoint, zoomed.toScreen(viewport.toCanvas(focalPoint)))
    }

    @Test fun fitCentersBoundsAndRespectsPadding() {
        val viewport = CanvasViewport.fit(
            bounds = CanvasBounds(100f, 50f, 300f, 150f),
            viewportSize = CanvasSize(1000f, 600f),
            padding = 100f,
            minZoom = .5f,
            maxZoom = 5f
        )

        assertEquals(4f, viewport.zoom, .001f)
        assertEquals(CanvasPoint(500f, 300f), viewport.toScreen(CanvasPoint(200f, 100f)))
    }

    @Test fun hitTestingUsesInverseViewportAndTopmostNode() {
        val nodes = listOf(
            CanvasNode("back", CanvasPoint(10f, 10f), CanvasSize(100f, 100f)),
            CanvasNode("front", CanvasPoint(50f, 50f), CanvasSize(100f, 100f))
        )
        val viewport = CanvasViewport(zoom = 2f, pan = CanvasPoint(20f, 20f))

        assertEquals("front", hitTestCanvasNodes(CanvasPoint(140f, 140f), viewport, nodes)?.id)
        assertNull(hitTestCanvasNodes(CanvasPoint(10f, 10f), viewport, nodes))
    }

    @Test fun endpointsAreDerivedFromValidGraphEdgesAndKeepIsolatedNodes() {
        val endpoints = deriveCanvasEndpoints(
            nodeIds = setOf("a", "b", "c", "isolated"),
            edges = listOf(
                CanvasEdge("a", "b"),
                CanvasEdge("b", "c"),
                CanvasEdge("missing", "a")
            )
        )

        assertEquals(setOf("a", "isolated"), endpoints.startNodeIds)
        assertEquals(setOf("c", "isolated"), endpoints.endNodeIds)
    }

    @Test fun initialLayoutWrapsEveryFiveScreensInStableLogicalCoordinates() {
        assertEquals(CanvasPoint(0f, 0f), InitialCanvasLayout.positionFor(0))
        assertEquals(CanvasPoint(1_600f, 0f), InitialCanvasLayout.positionFor(4))
        assertEquals(CanvasPoint(0f, 760f), InitialCanvasLayout.positionFor(5))
    }

    @Test fun initialLayoutHonoursRequestedColumnCount() {
        assertEquals(CanvasPoint(2_000f, 0f), InitialCanvasLayout.positionFor(5, columns = 6))
        assertEquals(CanvasPoint(0f, 760f), InitialCanvasLayout.positionFor(6, columns = 6))
    }

    @Test fun autoLayoutPlacesBranchesInColumnsByGraphDepth() {
        val positions = autoLayoutCanvas(listOf("start", "first", "second", "end"), listOf(
            CanvasEdge("start", "first"), CanvasEdge("start", "second"),
            CanvasEdge("first", "end"), CanvasEdge("second", "end")
        ))

        assertEquals(CanvasPoint(0f, 0f), positions["start"])
        assertEquals(CanvasPoint(400f, 0f), positions["first"])
        assertEquals(CanvasPoint(400f, 760f), positions["second"])
        assertEquals(CanvasPoint(800f, 0f), positions["end"])
    }

    @Test fun connectorEndpointUsesNearestDestinationBoundary() {
        val endpoint = nearestPointOnCanvasBounds(CanvasPoint(10f, 150f), CanvasBounds(100f, 100f, 300f, 300f))
        assertEquals(CanvasPoint(100f, 150f), endpoint)
    }
}
