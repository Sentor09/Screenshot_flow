package com.flowsnap.data

import android.content.Context
import java.io.File
import java.util.UUID

/**
 * Owns FlowSnap's app-private screenshot files.
 *
 * Files live in internal storage, so no storage permission is needed and no
 * other app can browse them. Room stores the absolute file path in
 * [ScreenEntity.imagePath] for each kept capture.
 */
object ScreenshotStorage {
    private const val ROOT_FOLDER = "flowsnap/screenshots"

    fun newCaptureFile(context: Context, flowId: String): File {
        val flowFolder = File(context.filesDir, "$ROOT_FOLDER/$flowId")
        check(flowFolder.exists() || flowFolder.mkdirs()) {
            "Unable to create FlowSnap screenshot storage"
        }
        return File(flowFolder, "${System.currentTimeMillis()}-${UUID.randomUUID()}.png")
    }

    fun deleteFlowFolder(context: Context, flowId: String) {
        File(context.filesDir, "$ROOT_FOLDER/$flowId").deleteRecursively()
    }

    /** Best-effort startup reconciliation after a failed capture or file delete. */
    fun deleteUnreferencedFiles(context: Context, referencedPaths: Set<String>) {
        val root = File(context.filesDir, ROOT_FOLDER)
        root.walkTopDown()
            .filter { it.isFile && it.extension.equals("png", ignoreCase = true) }
            .filter { it.absolutePath !in referencedPaths }
            .forEach { runCatching { it.delete() } }
        root.walkBottomUp().filter { it.isDirectory && it != root }.forEach { directory ->
            if (directory.list()?.isEmpty() == true) runCatching { directory.delete() }
        }
    }
}
