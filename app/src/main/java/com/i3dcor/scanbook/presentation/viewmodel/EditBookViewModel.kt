package com.i3dcor.scanbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i3dcor.scanbook.data.repository.CompositeBookLookupRepository
import com.i3dcor.scanbook.data.repository.GoogleBooksRepository
import com.i3dcor.scanbook.data.repository.InMemoryIsbnRepository
import com.i3dcor.scanbook.data.repository.OpenLibraryBookRepository
import com.i3dcor.scanbook.domain.model.BookNotFoundException
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.BookLookupRepository
import com.i3dcor.scanbook.domain.repository.CoverDownloadScheduler
import com.i3dcor.scanbook.domain.repository.IsbnRepository
import com.i3dcor.scanbook.presentation.state.EditBookUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class EditBookViewModel(
    initialBook: ScannedIsbn?,
    private val repository: IsbnRepository = InMemoryIsbnRepository.instance,
    private val lookupRepository: BookLookupRepository = CompositeBookLookupRepository(
        listOf(
            OpenLibraryBookRepository(),
            GoogleBooksRepository()
        )
    ),
    private val coverScheduler: CoverDownloadScheduler? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(mapToUiState(initialBook))
    val uiState: StateFlow<EditBookUiState> = _uiState.asStateFlow()

    private var lookupJob: Job? = null
    private var capturedPhotoPath: String? = null

    fun onIsbnChange(value: String) {
        _uiState.update { it.copy(isbn = value, searchError = null) }
        scheduleLookup(value)
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onAuthorChange(value: String) {
        _uiState.update { it.copy(author = value) }
    }

    fun onGenreChange(value: String) {
        _uiState.update { it.copy(genre = value) }
    }

    fun onLocalCoverCaptured(path: String) {
        capturedPhotoPath = path
        _uiState.update { it.copy(coverLocalPath = path) }
    }

    fun discardCapturedPhoto() {
        capturedPhotoPath?.let { File(it).delete() }
        capturedPhotoPath = null
        _uiState.update { it.copy(coverLocalPath = null) }
    }

    fun onSave(onComplete: () -> Unit) {
        val state = _uiState.value
        val scannedIsbn = ScannedIsbn(
            isbn = state.isbn,
            title = state.title.ifBlank { null },
            author = state.author.ifBlank { null },
            genre = state.genre.ifBlank { null },
            price = state.price.toDoubleOrNull(),
            condition = state.condition.ifBlank { null },
            coverUrl = state.coverUrl,
            coverLocalPath = state.coverLocalPath
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(scannedIsbn)
            capturedPhotoPath = null
            if (scannedIsbn.coverLocalPath == null) {
                scannedIsbn.coverUrl?.let { url ->
                    coverScheduler?.scheduleCoverDownload(scannedIsbn.isbn, url)
                }
            }
            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    /**
     * Programa una búsqueda por ISBN con debounce de 1 segundo.
     * Cancela cualquier búsqueda pendiente anterior.
     * Solo busca si el ISBN tiene al menos 10 caracteres (ISBN-10 o ISBN-13).
     */
    private fun scheduleLookup(isbn: String) {
        lookupJob?.cancel()

        val cleanIsbn = isbn.replace("-", "").replace(" ", "")
        if (cleanIsbn.length < 10) {
            _uiState.update { it.copy(isSearching = false, searchError = null) }
            return
        }

        lookupJob = viewModelScope.launch {
            delay(1000L)
            lookupBookData(cleanIsbn)
        }
    }

    /**
     * Busca los datos del libro por ISBN y rellena solo los campos vacíos.
     */
    private suspend fun lookupBookData(isbn: String) {
        _uiState.update { it.copy(isSearching = true, searchError = null) }

        lookupRepository.lookupByIsbn(isbn)
            .onSuccess { book ->
                _uiState.update { current ->
                    current.copy(
                        title = current.title.ifBlank { book.title.orEmpty() },
                        author = current.author.ifBlank { book.author.orEmpty() },
                        genre = current.genre.ifBlank { book.genre.orEmpty() },
                        coverUrl = current.coverUrl ?: book.coverUrl,
                        isSearching = false,
                        searchError = null
                    )
                }
            }
            .onFailure { exception ->
                _uiState.update { current ->
                    current.copy(
                        isSearching = false,
                        searchError = mapExceptionToUserMessage(exception)
                    )
                }
            }
    }

    private fun mapExceptionToUserMessage(exception: Throwable): String {
        return when (exception) {
            is BookNotFoundException -> "Libro no encontrado"
            is UnknownHostException -> "Sin conexión a Internet"
            is SocketTimeoutException -> "Tiempo de espera agotado"
            else -> "Error al buscar el libro"
        }
    }

    private fun mapToUiState(book: ScannedIsbn?): EditBookUiState {
        if (book == null) return EditBookUiState()
        return EditBookUiState(
            isbn = book.isbn,
            title = book.title.orEmpty(),
            author = book.author.orEmpty(),
            genre = book.genre.orEmpty(),
            price = book.price?.toString().orEmpty(),
            condition = book.condition ?: "Good",
            coverUrl = book.coverUrl,
            coverLocalPath = book.coverLocalPath
        )
    }
}
