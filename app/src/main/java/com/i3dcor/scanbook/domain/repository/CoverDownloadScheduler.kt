package com.i3dcor.scanbook.domain.repository

/**
 * Interfaz de dominio para programar la descarga en background de portadas de libros.
 * Kotlin puro, sin dependencias de Android.
 */
interface CoverDownloadScheduler {
    fun scheduleCoverDownload(isbn: String, coverUrl: String)
}
