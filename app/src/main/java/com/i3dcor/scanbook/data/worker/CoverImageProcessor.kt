package com.i3dcor.scanbook.data.worker

import java.io.File

/**
 * Descarga, escala y guarda una imagen de portada en disco.
 * Interfaz extraída para permitir tests unitarios del Worker sin dependencias Android.
 */
interface CoverImageProcessor {
    /** Descarga la imagen en [url], la escala y la guarda en [destFile]. Lanza excepción en fallo. */
    fun downloadScaleAndSave(url: String, destFile: File)
}
