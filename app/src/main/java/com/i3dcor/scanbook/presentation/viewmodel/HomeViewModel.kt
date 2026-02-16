package com.i3dcor.scanbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.IsbnRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla Home.
 * Carga la lista de libros desde el repositorio y la expone como StateFlow.
 */
class HomeViewModel(
    private val repository: IsbnRepository
) : ViewModel() {

    private val _books = MutableStateFlow<List<ScannedIsbn>>(emptyList())
    val books: StateFlow<List<ScannedIsbn>> = _books.asStateFlow()

    init {
        refresh()
    }

    /**
     * Recarga la lista de libros desde el repositorio.
     * Llamar al volver a Home para reflejar cambios (ej: tras guardar un libro).
     */
    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getAll()
            _books.value = result
        }
    }

    /**
     * Elimina un libro por su ISBN y refresca la lista.
     */
    fun deleteBook(isbn: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(isbn)
            val result = repository.getAll()
            _books.value = result
        }
    }
}
