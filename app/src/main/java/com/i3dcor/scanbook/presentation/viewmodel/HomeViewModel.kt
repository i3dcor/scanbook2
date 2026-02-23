package com.i3dcor.scanbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.CoverDownloadScheduler
import com.i3dcor.scanbook.domain.repository.IsbnRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla Home.
 * Carga la lista de libros desde el repositorio y la expone como StateFlow.
 */
class HomeViewModel(
    private val repository: IsbnRepository,
    private val coverScheduler: CoverDownloadScheduler? = null
) : ViewModel() {

    private val _books = MutableStateFlow<List<ScannedIsbn>>(emptyList())
    val books: StateFlow<List<ScannedIsbn>> = _books.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Lista de libros filtrada en tiempo real por ISBN, título o autor.
     * Si la query está vacía, devuelve todos los libros.
     */
    val filteredBooks: StateFlow<List<ScannedIsbn>> = combine(_books, _searchQuery) { books, query ->
        if (query.isBlank()) {
            books
        } else {
            val lowerQuery = query.lowercase()
            books.filter { book ->
                book.isbn.lowercase().contains(lowerQuery) ||
                    book.title.orEmpty().lowercase().contains(lowerQuery) ||
                    book.author.orEmpty().lowercase().contains(lowerQuery)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    /**
     * Actualiza la query de búsqueda para filtrar libros en tiempo real.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
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
     * Guarda un libro en la base de datos y refresca la lista.
     */
    fun addBook(book: ScannedIsbn) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(book)
            val result = repository.getAll()
            _books.value = result
        }
    }

    /**
     * Re-encola la descarga de portadas para libros guardados que aún no tienen imagen local.
     * Útil al arrancar la app para recuperar descargas fallidas o pendientes.
     */
    fun downloadPendingCovers() {
        val scheduler = coverScheduler ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAll()
                .filter { it.coverUrl != null && it.coverLocalPath == null }
                .forEach { book -> scheduler.scheduleCoverDownload(book.isbn, book.coverUrl!!) }
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
