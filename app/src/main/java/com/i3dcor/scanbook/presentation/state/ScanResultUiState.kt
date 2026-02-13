package com.i3dcor.scanbook.presentation.state

import com.i3dcor.scanbook.domain.model.ScannedIsbn

/**
 * Estado de la UI para la pantalla de resultado de escaneo.
 * Representa los diferentes estados posibles de la búsqueda de datos del libro.
 */
data class ScanResultUiState(
    val scannedIsbn: ScannedIsbn,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    companion object {
        /**
         * Crea un estado inicial con solo el ISBN escaneado.
         */
        fun initial(isbn: String) = ScanResultUiState(
            scannedIsbn = ScannedIsbn(isbn = isbn),
            isLoading = true
        )
    }
}
