package com.flowsnap

import org.junit.Assert.assertEquals
import org.junit.Test

class CaptureCropTest {
    @Test fun validCropPreservesInBoundsSelection() {
        val crop = CaptureCrop(.12f, .18f, .84f, .91f).valid()

        assertEquals(.12f, crop.left, .0001f)
        assertEquals(.18f, crop.top, .0001f)
        assertEquals(.84f, crop.right, .0001f)
        assertEquals(.91f, crop.bottom, .0001f)
    }

    @Test fun validCropClampsEdgesAndMaintainsMinimumSize() {
        val crop = CaptureCrop(-.3f, .98f, .01f, .2f).valid()

        assertEquals(0f, crop.left, .0001f)
        assertEquals(.92f, crop.top, .0001f)
        assertEquals(.08f, crop.right, .0001f)
        assertEquals(1f, crop.bottom, .0001f)
    }

    @Test fun validCropExpandsCrossedCornersToMinimumSelection() {
        val crop = CaptureCrop(.6f, .5f, .4f, .3f).valid()

        assertEquals(.6f, crop.left, .0001f)
        assertEquals(.5f, crop.top, .0001f)
        assertEquals(.68f, crop.right, .0001f)
        assertEquals(.58f, crop.bottom, .0001f)
    }
}
