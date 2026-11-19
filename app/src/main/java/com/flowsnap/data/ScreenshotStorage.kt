package com.flowsnap.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.util.UUID

/**
 * Owns FlowSnap screenshot storage.
 *
 * New files live in a SAF folder chosen by the user. Room stores each
 * persisted content URI in [ScreenEntity.imagePath]. Legacy private paths are
 * kept readable and deletable for existing diagrams.
 */
object ScreenshotStorage {
    private const val PREFS = "flowsnap_capture_storage"
    private const val TREE_URI = "capture_tree_uri"
    private const val ROOT_FOLDER = "FlowSnap"
    private const val SCREENSHOTS_FOLDER = "screenshots"

    fun savedTreeUri(context: Context): Uri? = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(TREE_URI, null)?.let(Uri::parse)

    fun hasCaptureDestination(context: Context): Boolean = savedTreeUri(context)?.let {
        DocumentFile.fromTreeUri(context, it)?.canWrite() == true
    } == true

    fun saveCaptureDestination(context: Context, treeUri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        check(DocumentFile.fromTreeUri(context, treeUri)?.canWrite() == true) {
            "The selected folder is not writable"
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(TREE_URI, treeUri.toString()).apply()
    }

    fun newCaptureUri(context: Context, flowId: String): Uri {
        val tree = savedTreeUri(context) ?: error("Choose a screenshot folder before capturing")
        val root = DocumentFile.fromTreeUri(context, tree) ?: error("The selected screenshot folder is unavailable")
        val flowFolder = root.childDirectory(ROOT_FOLDER)
            .childDirectory(SCREENSHOTS_FOLDER)
            .childDirectory(flowId)
        val file = flowFolder.createFile("image/png", "${System.currentTimeMillis()}-${UUID.randomUUID()}.png")
            ?: error("Unable to create screenshot file")
        return file.uri
    }

    fun delete(context: Context, location: String) {
        if (location.startsWith("content://")) {
            DocumentFile.fromSingleUri(context, Uri.parse(location))?.delete()
        } else {
            File(location).delete()
        }
    }

    fun deleteFlowFolder(context: Context, flowId: String) {
        val tree = savedTreeUri(context) ?: return
        DocumentFile.fromTreeUri(context, tree)
            ?.findFile(ROOT_FOLDER)
            ?.findFile(SCREENSHOTS_FOLDER)
            ?.findFile(flowId)
            ?.delete()
    }

    /** Best-effort reconciliation after a failed capture or file delete. */
    fun deleteUnreferencedFiles(context: Context, referencedLocations: Set<String>) {
        val tree = savedTreeUri(context) ?: return
        val root = DocumentFile.fromTreeUri(context, tree)
            ?.findFile(ROOT_FOLDER)
            ?.findFile(SCREENSHOTS_FOLDER) ?: return
        root.listFiles().forEach { flowFolder ->
            flowFolder.listFiles().forEach { file ->
                if (file.uri.toString() !in referencedLocations) runCatching { file.delete() }
            }
            if (flowFolder.listFiles().isEmpty()) runCatching { flowFolder.delete() }
        }
    }

    private fun DocumentFile.childDirectory(name: String): DocumentFile =
        findFile(name) ?: createDirectory(name) ?: error("Unable to create $name folder")
}
