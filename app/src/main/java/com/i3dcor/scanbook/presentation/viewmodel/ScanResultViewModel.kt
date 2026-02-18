package com.i3dcor.scanbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i3dcor.scanbook.data.repository.CompositeBookLookupRepository
import com.i3dcor.scanbook.data.repository.GoogleBooksRepository
import com.i3dcor.scanbook.data.repository.InMemoryIsbnRepository
import com.i3dcor.scanbook.data.repository.OpenLibraryBookRepository
import com.i3dcor.scanbook.domain.model.BookNotFoundException
import com.i3dcor.scanbook.domain.repository.BookLookupRepository
import com.i3dcor.scanbook.domain.repository.IsbnRepository
import com.i3dcor.scanbook.presentation.state.ScanResultUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * ViewModel para la pantalla de resultado de escaneo.
 * Comprueba primero si el libro ya existe en la base de datos local.
 * Si no existe, busca los datos en internet con fallback automático.
 */
class ScanResultViewModel(
    private val isbn: String,
    private val lookupRepository: BookLookupRepository = CompositeBookLookupRepository(
        listOf(
            OpenLibraryBookRepository(),
            GoogleBooksRepository()
        )
    ),
    private val isbnRepository: IsbnRepository = InMemoryIsbnRepository.instance
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScanResultUiState.initial(isbn))
    val uiState: StateFlow<ScanResultUiState> = _uiState.asStateFlow()
    
    init {
        checkLocalThenLookup()
    }
    
    /**
     * Comprueba si el ISBN ya existe en la base de datos local.
     * Si existe, muestra los datos guardados sin buscar en internet.
     * Si no existe, busca en internet.
     */
    private fun checkLocalThenLookup() {
        viewModelScope.launch {
            val existingBook = withContext(Dispatchers.IO) {
                isbnRepository.getByIsbn(isbn)
            }
            
            if (existingBook != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        scannedIsbn = existingBook,
                        isLoading = false,
                        error = null,
                        alreadyExists = true
                    )
                }
            } else {
                lookupBookData()
            }
        }
    }
    
    /**
     * Busca los datos del libro por ISBN en segundo plano.
     * Actualiza el estado de la UI según el resultado.
     */
    private suspend fun lookupBookData() {
        lookupRepository.lookupByIsbn(isbn)
            .onSuccess { book ->
                _uiState.update { currentState ->
                    currentState.copy(
                        scannedIsbn = book,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .onFailure { exception ->
                val errorMessage = mapExceptionToUserMessage(exception)
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
    }
    
    /**
     * Reintenta la búsqueda de datos del libro.
     */
    fun retry() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            lookupBookData()
        }
    }
    
    /**
     * Mapea excepciones técnicas a mensajes de error legibles para el usuario.
     */
    private fun mapExceptionToUserMessage(exception: Throwable): String {
        return when (exception) {
            is BookNotFoundException -> "Libro no encontrado"
            is UnknownHostException -> "Sin conexión a Internet"
            is SocketTimeoutException -> "Tiempo de espera agotado"
            else -> "Error al buscar el libro"
        }
    }
}
