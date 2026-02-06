package com.i3dcor.scanbook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Un componente de item de lista para mostrar la información de un libro.
 *
 * Este componente es stateless y altamente personalizable para su uso en listas.
 * Sigue el principio de state hoisting, delegando el estado y las acciones al llamador.
 *
 * @param title Título del libro.
 * @param author Autor del libro.
 * @param onItemClick Callback que se invoca al hacer clic en el item.
 * @param onMoreActionClick Callback para el menú de acciones (icono de tres puntos).
 * @param modifier Modificador para personalizar el estilo y layout.
 * @param imageContent Un slot de composable para mostrar la portada del libro. Permite
 *                     inyectar un icono, una imagen de red (con Coil/Glide) o cualquier otro
 *                     composable personalizado.
 */
@Composable
fun BookListItem(
    title: String,
    author: String,
    onItemClick: () -> Unit,
    onMoreActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageContent: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onItemClick),
        color = Color(0xFF2C2C2E) // Color de fondo oscuro de la captura
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contenedor para la imagen
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE5E5EA) // Fondo claro para la imagen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    imageContent()
                }
            }

            // Columna para título y autor
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Botón de más acciones
            IconButton(onClick = onMoreActionClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Más acciones",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
private fun BookListItemPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        BookListItem(
            title = "Clean Code",
            author = "Robert C. Martin",
            onItemClick = { },
            onMoreActionClick = { }
        ) {
            // Para la preview, usamos el icono de libro por defecto como solicitaste.
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = "Portada del libro",
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF4A3C32)
            )
        }
    }
}
