package com.i3dcor.scanbook.data.worker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

class DefaultCoverImageProcessor : CoverImageProcessor {

    override fun downloadScaleAndSave(url: String, destFile: File) {
        val bitmap = URL(url).openStream().use { BitmapFactory.decodeStream(it) }
            ?: throw IOException("No se pudo decodificar la imagen de $url")
        val scaled = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true)
        bitmap.recycle()
        FileOutputStream(destFile).use { scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
        scaled.recycle()
    }

    private companion object {
        const val WIDTH = 100
        const val HEIGHT = 150
        const val QUALITY = 60
    }
}
