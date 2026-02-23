package com.i3dcor.scanbook.data.scheduler

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.i3dcor.scanbook.data.worker.DownloadCoverWorker
import com.i3dcor.scanbook.domain.repository.CoverDownloadScheduler

/**
 * Implementación de CoverDownloadScheduler usando WorkManager.
 * Encola una tarea de descarga en background con reintentos exponenciales.
 */
class WorkManagerCoverScheduler(private val context: Context) : CoverDownloadScheduler {
    override fun scheduleCoverDownload(isbn: String, coverUrl: String) {
        val inputData = Data.Builder()
            .putString(DownloadCoverWorker.KEY_ISBN, isbn)
            .putString(DownloadCoverWorker.KEY_COVER_URL, coverUrl)
            .build()
        val request = OneTimeWorkRequestBuilder<DownloadCoverWorker>()
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
