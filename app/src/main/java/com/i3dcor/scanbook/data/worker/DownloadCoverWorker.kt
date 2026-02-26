package com.i3dcor.scanbook.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.i3dcor.scanbook.data.local.ScanBookDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Worker que descarga, escala y comprime la portada de un libro.
 * Guarda el archivo en filesDir/covers/{isbn}.jpg y actualiza Room.
 * WorkManager reintenta con backoff exponencial en caso de fallo de red.
 */
class DownloadCoverWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val service = DownloadCoverService(
        imageProcessor = DefaultCoverImageProcessor(),
        pathUpdater = { isbn, path ->
            ScanBookDatabase.getInstance(applicationContext)
                .bookDao().updateCoverLocalPath(isbn, path)
        }
    )

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val isbn = inputData.getString(KEY_ISBN) ?: return@withContext Result.failure()
        val coverUrl = inputData.getString(KEY_COVER_URL) ?: return@withContext Result.failure()
        val coversDir = File(applicationContext.filesDir, "covers")
        service.processDownload(isbn, coverUrl, coversDir)
    }

    companion object {
        const val KEY_ISBN = "isbn"
        const val KEY_COVER_URL = "cover_url"
    }
}
