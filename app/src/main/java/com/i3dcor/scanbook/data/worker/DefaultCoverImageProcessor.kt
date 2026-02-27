package com.i3dcor.scanbook.data.worker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DefaultCoverImageProcessor : CoverImageProcessor {

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
        const val WIDTH      = 100
        const val HEIGHT     = 150
        const val QUALITY    = 60
        const val TIMEOUT_MS = 10_000
        const val MAX_BYTES  = 5L * 1024 * 1024   // 5 MB
    }
}
