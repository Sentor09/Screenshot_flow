package com.flowsnap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.flowsnap.data.ScreenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Small, bounded cache for renderer thumbnails. Full-resolution captures never decode on Compose's main thread. */
private object ScreenshotBitmapLoader {
    private val cache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
    }

    suspend fun load(context: Context, path: String, maxDimension: Int): Bitmap? = withContext(Dispatchers.IO) {
        val key = "$path@$maxDimension"
        cache.get(key) ?: BitmapFactory.Options().run {
            inJustDecodeBounds = true
            decode(context, path, this)
            if (outWidth <= 0 || outHeight <= 0) return@withContext null
            var sample = 1
            while (outWidth / sample > maxDimension || outHeight / sample > maxDimension) sample *= 2
            inJustDecodeBounds = false
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
            decode(context, path, this)?.also { cache.put(key, it) }
        }
    }

    private fun decode(context: Context, location: String, options: BitmapFactory.Options): Bitmap? =
        if (location.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(location))?.use { BitmapFactory.decodeStream(it, null, options) }
        } else BitmapFactory.decodeFile(location, options)

}

sealed interface ScreenshotBitmapState {
    data object Loading : ScreenshotBitmapState
    data class Ready(val bitmap: ImageBitmap) : ScreenshotBitmapState
    data object Failed : ScreenshotBitmapState
}

@Composable
@Suppress("ProduceStateDoesNotAssignValue") // Lint 8.7 misses ProducerScope.value assignments in this generic map builder.
fun rememberScreenBitmaps(screens: List<ScreenEntity>, maxDimension: Int): Map<String, ImageBitmap> {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val paths = screens.map { it.id to it.imagePath }
    return produceState<Map<String, ImageBitmap>>(emptyMap(), paths, maxDimension) {
        this.value = paths.mapNotNull { (id, path) -> ScreenshotBitmapLoader.load(context, path, maxDimension)?.let { id to it.asImageBitmap() } }.toMap()
    }.value
}

@Composable
@Suppress("ProduceStateDoesNotAssignValue") // See rememberScreenBitmaps above.
fun rememberScreenBitmapStates(screens: List<ScreenEntity>, maxDimension: Int): Map<String, ScreenshotBitmapState> {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val paths = screens.map { it.id to it.imagePath }
    val initial = paths.associate { it.first to ScreenshotBitmapState.Loading }
    return produceState<Map<String, ScreenshotBitmapState>>(initial, paths, maxDimension) {
        this.value = paths.associate { (id, path) ->
            id to (ScreenshotBitmapLoader.load(context, path, maxDimension)
                ?.let { ScreenshotBitmapState.Ready(it.asImageBitmap()) }
                ?: ScreenshotBitmapState.Failed)
        }
    }.value
}
