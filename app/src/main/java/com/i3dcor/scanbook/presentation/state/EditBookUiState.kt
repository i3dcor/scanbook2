package com.i3dcor.scanbook.presentation.state

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
