package com.flowsnap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
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

    suspend fun load(path: String, maxDimension: Int): Bitmap? = withContext(Dispatchers.IO) {
        val key = "$path@$maxDimension"
        cache.get(key) ?: BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
            if (outWidth <= 0 || outHeight <= 0) return@withContext null
            var sample = 1
            while (outWidth / sample > maxDimension || outHeight / sample > maxDimension) sample *= 2
            inJustDecodeBounds = false
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeFile(path, this)?.also { cache.put(key, it) }
        }
    }
}

@Composable
fun rememberScreenBitmaps(screens: List<ScreenEntity>, maxDimension: Int): Map<String, ImageBitmap> {
    val paths = screens.map { it.id to it.imagePath }
    return produceState<Map<String, ImageBitmap>>(emptyMap(), paths, maxDimension) {
        value = paths.mapNotNull { (id, path) -> ScreenshotBitmapLoader.load(path, maxDimension)?.let { id to it.asImageBitmap() } }.toMap()
    }.value
}
