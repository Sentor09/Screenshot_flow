package com.flowsnap

/** Geometry shared by capture review and the lightbox pointer editor. */
data class ScreenshotBounds(val left: Float, val top: Float, val width: Float, val height: Float) {
    fun pointFor(normalizedX: Float, normalizedY: Float) = Pair(
        left + width * normalizedX.coerceIn(0f, 1f),
        top + height * normalizedY.coerceIn(0f, 1f)
    )

    fun normalize(x: Float, y: Float) = Pair(
        ((x - left) / width).coerceIn(0f, 1f),
        ((y - top) / height).coerceIn(0f, 1f)
    )
}

fun fittedScreenshotBounds(
    containerWidth: Float,
    containerHeight: Float,
    imageWidth: Int,
    imageHeight: Int
): ScreenshotBounds {
    if (containerWidth <= 0f || containerHeight <= 0f || imageWidth <= 0 || imageHeight <= 0) {
        return ScreenshotBounds(0f, 0f, containerWidth.coerceAtLeast(1f), containerHeight.coerceAtLeast(1f))
    }
    val scale = minOf(containerWidth / imageWidth, containerHeight / imageHeight)
    val width = imageWidth * scale
    val height = imageHeight * scale
    return ScreenshotBounds((containerWidth - width) / 2f, (containerHeight - height) / 2f, width, height)
}
