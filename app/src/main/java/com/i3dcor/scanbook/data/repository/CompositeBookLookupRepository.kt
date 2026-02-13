package com.i3dcor.scanbook.data.repository

import com.i3dcor.scanbook.domain.model.BookNotFoundException
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.BookLookupRepository

/**
 * Repository compuesto que intenta múltiples fuentes de datos en orden.
 * Implementa el patrón Chain of Responsibility para fallback entre APIs.
 * 
 * Comportamiento:
 * - Si una fuente retorna éxito → devuelve el resultado
 * - Si una fuente retorna BookNotFoundException → intenta la siguiente fuente
 * - Si una fuente retorna otro error (red, timeout) → propaga el error inmediatamente
 */
class CompositeBookLookupRepository(
    private val repositories: List<BookLookupRepository>
) : BookLookupRepository {

    override suspend fun lookupByIsbn(isbn: String): Result<ScannedIsbn> {
        var lastException: Throwable? = null

        for (repository in repositories) {
            val result = repository.lookupByIsbn(isbn)
            
            if (result.isSuccess) {
                return result
            }
            
            val exception = result.exceptionOrNull()
            lastException = exception
            
            // Solo continuar al siguiente si es "libro no encontrado"
            // Otros errores (red, timeout, etc.) se propagan inmediatamente
            if (exception !is BookNotFoundException) {
                return result
            }
        }

        // Todas las fuentes retornaron BookNotFoundException
        return Result.failure(lastException ?: BookNotFoundException(isbn))
    }
}
