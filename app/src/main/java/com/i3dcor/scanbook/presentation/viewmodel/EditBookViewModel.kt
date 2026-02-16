package com.i3dcor.scanbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.i3dcor.scanbook.data.repository.InMemoryIsbnRepository
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.IsbnRepository
import com.i3dcor.scanbook.presentation.state.EditBookUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditBookViewModel(
    initialBook: ScannedIsbn?,
    private val repository: IsbnRepository = InMemoryIsbnRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(mapToUiState(initialBook))
    val uiState: StateFlow<EditBookUiState> = _uiState.asStateFlow()

    fun onIsbnChange(value: String) {
        _uiState.update { it.copy(isbn = value) }
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

    fun onPriceChange(value: String) {
        _uiState.update { it.copy(price = value) }
    }

    fun onConditionChange(value: String) {
        _uiState.update { it.copy(condition = value) }
    }

    fun onSave() {
        val state = _uiState.value
        val scannedIsbn = ScannedIsbn(
            isbn = state.isbn,
            title = state.title.ifBlank { null },
            author = state.author.ifBlank { null },
            genre = state.genre.ifBlank { null },
            price = state.price.toDoubleOrNull(),
            condition = state.condition.ifBlank { null },
            coverUrl = state.coverUrl
        )
        repository.insert(scannedIsbn)
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
            coverUrl = book.coverUrl
        )
    }
}
