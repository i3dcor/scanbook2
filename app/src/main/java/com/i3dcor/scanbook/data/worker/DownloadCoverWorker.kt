package com.i3dcor.scanbook.data.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.i3dcor.scanbook.data.local.ScanBookDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Worker que descarga, escala y comprime la portada de un libro.
 * Guarda el archivo en filesDir/covers/{isbn}.jpg y actualiza Room.
 * WorkManager reintenta con backoff exponencial en caso de fallo de red.
 */
class DownloadCoverWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val KEY_ISBN = "isbn"
        const val KEY_COVER_URL = "cover_url"
        private const val WIDTH = 100
        private const val HEIGHT = 150
        private const val QUALITY = 60
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val isbn = inputData.getString(KEY_ISBN) ?: return@withContext Result.failure()
        val coverUrl = inputData.getString(KEY_COVER_URL) ?: return@withContext Result.failure()
        try {
            val bitmap = URL(coverUrl).openStream().use { BitmapFactory.decodeStream(it) }
                ?: return@withContext Result.failure()
            val scaled = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true)
            bitmap.recycle()
            val coversDir = File(applicationContext.filesDir, "covers").also { it.mkdirs() }
            val file = File(coversDir, "$isbn.jpg")
            FileOutputStream(file).use { scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
            scaled.recycle()
            ScanBookDatabase.getInstance(applicationContext)
                .bookDao().updateCoverLocalPath(isbn, file.absolutePath)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
