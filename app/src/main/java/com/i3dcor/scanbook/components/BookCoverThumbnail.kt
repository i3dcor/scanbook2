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

/**
 * Miniatura de la portada de un libro.
 *
 * Componente reutilizable que muestra la imagen de portada desde una URL
 * o un icono placeholder si no hay URL disponible.
 *
 * @param coverUrl URL de la portada del libro (nullable).
 * @param modifier Modificador para personalizar el estilo y layout.
 */
@Composable
fun BookCoverThumbnail(
    coverUrl: String?,
    modifier: Modifier = Modifier
) {
    if (coverUrl != null) {
        AsyncImage(
            model = coverUrl,
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
