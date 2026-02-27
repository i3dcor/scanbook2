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
    /**
     * Descarga y guarda la portada de un libro, luego actualiza Room con la ruta local.
     *
     * Incluye protección contra path traversal: valida que el archivo destino
     * quede dentro de [coversDir] antes de escribir.
     *
     * @param isbn ISBN del libro (usado como nombre de archivo, previamente saneado)
     * @param coverUrl URL HTTPS de la imagen de portada
     * @param coversDir Directorio local donde se guardan las portadas
     * @return [Result.success] si la descarga fue exitosa, [Result.retry] en cualquier error
     */
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
