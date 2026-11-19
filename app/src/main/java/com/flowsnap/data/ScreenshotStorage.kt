package com.flowsnap.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Owns FlowSnap screenshot storage.
 *
 * New files live in private app storage, so capturing never requires a user
 * to choose or create a device folder. Room stores each file path in
 * [ScreenEntity.imagePath].
 */
object ScreenshotStorage {
    private const val ROOT_FOLDER = "flowsnap/screenshots"

    fun newCaptureFile(context: Context, flowId: String): File {
        val flowFolder = File(captureRoot(context), flowId)
        check(flowFolder.exists() || flowFolder.mkdirs()) { "Unable to create private screenshot storage" }
        return File(flowFolder, "${System.currentTimeMillis()}-${UUID.randomUUID()}.png")
    }

    fun delete(context: Context, location: String) {
        if (location.startsWith("content://")) {
            context.contentResolver.delete(Uri.parse(location), null, null)
        } else {
            File(location).delete()
        }
    }

    fun deleteFlowFolder(context: Context, flowId: String) {
        File(captureRoot(context), flowId).deleteRecursively()
    }

    /** Best-effort reconciliation after a failed capture or file delete. */
    fun deleteUnreferencedFiles(context: Context, referencedLocations: Set<String>) {
        val root = captureRoot(context)
        if (!root.exists()) return
        root.walkBottomUp().forEach { file ->
            when {
                file.isFile && file.absolutePath !in referencedLocations -> runCatching { file.delete() }
                file.isDirectory && file != root && file.listFiles().isNullOrEmpty() -> runCatching { file.delete() }
            }
        }
    }

    private fun captureRoot(context: Context) = File(context.filesDir, ROOT_FOLDER)
}
