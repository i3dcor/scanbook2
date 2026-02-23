package com.i3dcor.scanbook.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

/**
 * Miniatura de la portada de un libro.
 *
 * Prioriza el archivo local sobre la URL remota para funcionar sin internet.
 *
 * @param coverUrl URL remota de la portada (nullable).
 * @param coverLocalPath Ruta absoluta al archivo local comprimido (nullable).
 * @param modifier Modificador para personalizar el estilo y layout.
 */
@Composable
fun BookCoverThumbnail(
    coverUrl: String?,
    coverLocalPath: String? = null,
    modifier: Modifier = Modifier
) {
    val imageModel: Any? = when {
        coverLocalPath != null -> File(coverLocalPath)
        coverUrl != null -> coverUrl
        else -> null
    }
    if (imageModel != null) {
        AsyncImage(
            model = imageModel,
            contentDescription = "Portada del libro",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = "Portada del libro",
            modifier = modifier.size(32.dp),
            tint = Color.Gray
        )
    }
}
