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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import com.i3dcor.scanbook.BuildConfig
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * ViewModel para la pantalla de edición/creación de un libro.
 *
 * Lanza una búsqueda automática de metadatos con debounce de 1 s al cambiar el ISBN.
 * Al guardar, si el libro no tiene portada local pero sí [coverUrl], encola su descarga
 * en background vía [coverScheduler].
 */
class EditBookViewModel(
    initialBook: ScannedIsbn?,
    private val repository: IsbnRepository = InMemoryIsbnRepository.instance,
    private val lookupRepository: BookLookupRepository = CompositeBookLookupRepository(
        listOf(
            OpenLibraryBookRepository(),
            GoogleBooksRepository()
        )
    ),
    private val coverScheduler: CoverDownloadScheduler? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(mapToUiState(initialBook))
    val uiState: StateFlow<EditBookUiState> = _uiState.asStateFlow()

    private var lookupJob: Job? = null
    private var capturedPhotoPath: String? = null

    /** Actualiza el ISBN y lanza una búsqueda automática de metadatos con debounce. */
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

    /**
     * Registra la ruta de una portada capturada por cámara en esta sesión.
     * [path] apunta a un archivo JPEG temporal en filesDir/covers/.
     */
    fun onLocalCoverCaptured(path: String) {
        capturedPhotoPath = path
        _uiState.update { it.copy(coverLocalPath = path) }
    }

    /**
     * Descarta la foto capturada en esta sesión y elimina el archivo temporal del disco.
     * No afecta portadas previamente guardadas en Room.
     */
    fun discardCapturedPhoto() {
        capturedPhotoPath?.let {
            val deleted = File(it).delete()
            if (!deleted && BuildConfig.DEBUG) Log.w(TAG, "No se pudo eliminar foto temporal: $it")
        }
        capturedPhotoPath = null
        _uiState.update { it.copy(coverLocalPath = null) }
    }

    /**
     * Elimina la portada local del libro: borra el archivo de disco, limpia [coverUrl]
     * y [coverLocalPath], dejando el libro sin imagen de portada.
     */
    fun deleteLocalCover() {
        _uiState.value.coverLocalPath?.let {
            val deleted = File(it).delete()
            if (!deleted && BuildConfig.DEBUG) Log.w(TAG, "No se pudo eliminar portada local: $it")
        }
        capturedPhotoPath = null
        _uiState.update { it.copy(coverUrl = null, coverLocalPath = null) }
    }

    /**
     * Persiste el libro en Room y, si no tiene portada local pero sí [coverUrl],
     * encola la descarga de portada en background vía [coverScheduler].
     *
     * @param onComplete Callback invocado en el hilo principal al finalizar.
     */
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
        viewModelScope.launch(ioDispatcher) {
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

    private companion object {
        const val TAG = "EditBookViewModel"
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
