package com.i3dcor.scanbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i3dcor.scanbook.data.repository.OpenLibraryBookRepository
import com.i3dcor.scanbook.domain.repository.BookLookupRepository
import com.i3dcor.scanbook.presentation.state.ScanResultUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de resultado de escaneo.
 * Maneja la búsqueda de datos del libro en segundo plano.
 */
class ScanResultViewModel(
    private val isbn: String,
    private val repository: BookLookupRepository = OpenLibraryBookRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScanResultUiState.initial(isbn))
    val uiState: StateFlow<ScanResultUiState> = _uiState.asStateFlow()
    
    init {
        lookupBookData()
    }
    
    /**
     * Busca los datos del libro por ISBN en segundo plano.
     * Actualiza el estado de la UI según el resultado.
     */
    private fun lookupBookData() {
        viewModelScope.launch {
            repository.lookupByIsbn(isbn)
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
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = "Could not fetch book data"
                        )
                    }
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
        lookupBookData()
    }
}
