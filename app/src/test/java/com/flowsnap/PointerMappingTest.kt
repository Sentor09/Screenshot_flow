package com.flowsnap

import org.junit.Assert.assertEquals
import org.junit.Test

class PointerMappingTest {
    @Test fun portraitScreenshotUsesHorizontalLetterboxing() {
        val bounds = fittedScreenshotBounds(1000f, 1000f, 500, 1000)
        assertEquals(250f, bounds.left, .001f)
        assertEquals(0f, bounds.top, .001f)
        assertEquals(500f, bounds.width, .001f)
        assertEquals(1000f, bounds.height, .001f)
    }

    @Test fun normalizedPointerRoundTripsThroughFittedBounds() {
        val bounds = fittedScreenshotBounds(1000f, 1200f, 1080, 2400)
        val (x, y) = bounds.pointFor(.75f, .2f)
        val (normalizedX, normalizedY) = bounds.normalize(x, y)
        assertEquals(.75f, normalizedX, .0001f)
        assertEquals(.2f, normalizedY, .0001f)
    }

    @Test fun normalizationClampsToTheScreenshotEdges() {
        val bounds = fittedScreenshotBounds(1000f, 1000f, 500, 1000)
        val (x, y) = bounds.normalize(0f, 2000f)
        assertEquals(0f, x, .001f)
        assertEquals(1f, y, .001f)
    }
}
