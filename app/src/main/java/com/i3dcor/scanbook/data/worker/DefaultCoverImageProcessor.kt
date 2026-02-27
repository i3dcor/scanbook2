package com.i3dcor.scanbook.data.worker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Implementación de [CoverImageProcessor] que descarga imágenes vía HTTPS,
 * las escala a [WIDTH]×[HEIGHT] px y las guarda en JPEG.
 *
 * Restricciones de seguridad:
 * - Solo acepta URLs HTTPS (rechaza HTTP)
 * - Limita la descarga a [MAX_BYTES] para evitar agotamiento de memoria
 */
class DefaultCoverImageProcessor : CoverImageProcessor {

    /**
     * Descarga la imagen en [url], la escala a [WIDTH]×[HEIGHT] px con calidad JPEG [QUALITY]%
     * y la guarda en [destFile]. Garantiza desconexión aunque ocurra un error.
     *
     * @throws IllegalArgumentException si [url] no empieza por "https://"
     * @throws IOException si la imagen no puede decodificarse o excede [MAX_BYTES]
     */
    override fun downloadScaleAndSave(url: String, destFile: File) {
        require(url.startsWith("https://")) { "Solo se permiten URLs HTTPS (recibida: $url)" }
        val connection = URL(url).openConnection() as HttpsURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout    = TIMEOUT_MS
        try {
            val bitmap = connection.inputStream.use { stream ->
                BitmapFactory.decodeStream(LimitedInputStream(stream, MAX_BYTES))
            } ?: throw IOException("No se pudo decodificar la imagen de $url")
            val scaled = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true)
            bitmap.recycle()
            FileOutputStream(destFile).use { scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
            scaled.recycle()
        } finally {
            connection.disconnect()
        }
    }

    /**
     * InputStream que lanza [IOException] si se intenta leer más de [maxBytes].
     * Previene agotamiento de memoria ante respuestas HTTP maliciosas o de tamaño inesperado.
     */
    private class LimitedInputStream(
        private val delegate: InputStream,
        private val maxBytes: Long
    ) : InputStream() {
        private var bytesRead = 0L

        override fun read(): Int {
            if (bytesRead >= maxBytes) throw IOException("Imagen supera el límite de ${maxBytes / (1024 * 1024)} MB")
            return delegate.read().also { if (it != -1) bytesRead++ }
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val remaining = maxBytes - bytesRead
            if (remaining <= 0) throw IOException("Imagen supera el límite de ${maxBytes / (1024 * 1024)} MB")
            val toRead = minOf(len.toLong(), remaining).toInt()
            return delegate.read(b, off, toRead).also { if (it > 0) bytesRead += it }
        }

        override fun close() = delegate.close()
    }

    private companion object {
        const val WIDTH      = 100   // px — miniatura suficiente para lista de libros
        const val HEIGHT     = 150   // px
        const val QUALITY    = 60    // JPEG — balance tamaño/calidad visual aceptable
        const val TIMEOUT_MS = 10_000
        const val MAX_BYTES  = 5L * 1024 * 1024   // 5 MB
    }
}
