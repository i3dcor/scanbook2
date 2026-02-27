package com.i3dcor.scanbook.data.worker

import androidx.work.ListenableWorker.Result
import java.io.File

/**
 * Lógica principal del Worker de descarga de portadas.
 * Extraída de DownloadCoverWorker para permitir tests unitarios sin WorkManager ni Android context.
 */
internal class DownloadCoverService(
    private val imageProcessor: CoverImageProcessor,
    private val pathUpdater: (isbn: String, path: String) -> Unit
) {
    fun processDownload(isbn: String, coverUrl: String, coversDir: File): Result =
        try {
            coversDir.mkdirs()
            val safeName = isbn.replace(Regex("[^A-Za-z0-9_-]"), "_") + ".jpg"
            val file = File(coversDir, safeName)
            require(file.canonicalPath.startsWith(coversDir.canonicalPath + File.separator)) {
                "Path traversal detectado para ISBN: $isbn"
            }
            imageProcessor.downloadScaleAndSave(coverUrl, file)
            pathUpdater(isbn, file.absolutePath)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
}
