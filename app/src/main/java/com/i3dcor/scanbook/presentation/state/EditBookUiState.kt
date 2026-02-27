package com.i3dcor.scanbook.presentation.state

/**
 * Estado de la UI para la pantalla de edición/creación de un libro.
 *
 * [price] se almacena como String para permitir edición libre en el campo de texto;
 * se convierte a Double al guardar. [condition] usa "Good" como valor por defecto
 * compatible con el modelo de dominio.
 *
 * [isSearching] indica que hay una búsqueda de metadatos por ISBN en curso.
 * [searchError] contiene el mensaje del último error de búsqueda, o null si fue exitosa.
 */
data class EditBookUiState(
    val isbn: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val price: String = "",
    val condition: String = "Good",
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val isSearching: Boolean = false,
    val searchError: String? = null
)
