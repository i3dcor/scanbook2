package com.i3dcor.scanbook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Una barra de búsqueda personalizada para la pantalla principal.
 *
 * Este componente es stateless y su estado (el texto de búsqueda) debe ser gestionado
 * desde el composable que lo llama (state hoisting).
 *
 * @param query El texto actual en la barra de búsqueda.
 * @param onQueryChange Callback que se invoca cuando el usuario modifica el texto.
 * @param onSearch Callback que se invoca cuando el usuario ejecuta una acción de búsqueda (ej. desde el teclado).
 * @param onExportClick Callback que se invoca al pulsar el botón de exportar.
 * @param modifier Modificador para personalizar el estilo y layout del componente.
 * @param placeholderText Texto que se muestra cuando la búsqueda está vacía.
 */
@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Search titles or authors"
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = CircleShape,
        color = Color(0xFF2C2C2E),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo de texto para la búsqueda
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch(query)
                        focusManager.clearFocus()
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholderText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Icono de cancelar búsqueda (visible solo cuando hay texto)
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Borrar búsqueda",
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = Color.Gray.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                            .padding(3.dp),
                        tint = Color.White
                    )
                }
            } else {
                // Botón de exportar (visible cuando no hay búsqueda activa)
                IconButton(
                    onClick = onExportClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Exportar colección",
                        modifier = Modifier.size(22.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
private fun HomeSearchBarPreview() {
    var query by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        HomeSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { },
            onExportClick = { }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
private fun HomeSearchBarWithTextPreview() {
    var query by remember { mutableStateOf("Clean Architecture") }
    Box(modifier = Modifier.padding(16.dp)) {
        HomeSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { },
            onExportClick = { }
        )
    }
}
