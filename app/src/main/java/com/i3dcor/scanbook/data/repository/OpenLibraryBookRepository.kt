package com.i3dcor.scanbook.data.repository

import com.i3dcor.scanbook.data.network.OpenLibraryApi
import com.i3dcor.scanbook.data.network.RetrofitClient
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.BookLookupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación de BookLookupRepository usando Open Library API.
 * Busca información de libros por ISBN y mapea los resultados al modelo de dominio.
 */
class OpenLibraryBookRepository(
    private val api: OpenLibraryApi = RetrofitClient.openLibraryApi
) : BookLookupRepository {
    
    companion object {
        private const val COVER_BASE_URL = "https://covers.openlibrary.org/b/isbn/"
        private const val COVER_SIZE_LARGE = "-L.jpg"
    }
    
    override suspend fun lookupByIsbn(isbn: String): Result<ScannedIsbn> {
        return withContext(Dispatchers.IO) {
            try {
                // Primera llamada: obtener datos del libro
                val bookDto = api.getBookByIsbn(isbn)
                
                // Segunda llamada: obtener nombre del autor (si existe)
                val authorName = bookDto.authors?.firstOrNull()?.key?.let { authorKey ->
                    fetchAuthorName(authorKey)
                }
                
                // Construir URL de portada
                val coverUrl = buildCoverUrl(isbn)
                
                // Extraer género del primer subject si existe
                val genre = bookDto.subjects?.firstOrNull()
                
                val scannedIsbn = ScannedIsbn(
                    isbn = isbn,
                    title = bookDto.title,
                    author = authorName,
                    genre = genre,
                    coverUrl = coverUrl
                )
                
                Result.success(scannedIsbn)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene el nombre del autor desde Open Library.
     * @param authorKey Key del autor (ej: "/authors/OL34184A")
     * @return Nombre del autor o null si falla
     */
    private suspend fun fetchAuthorName(authorKey: String): String? {
        return try {
            // El key viene con "/" al inicio, lo usamos directamente
            val cleanKey = authorKey.removePrefix("/")
            val authorDto = api.getAuthor(cleanKey)
            authorDto.name ?: authorDto.personalName
        } catch (e: Exception) {
            // Si falla la obtención del autor, no bloqueamos
            null
        }
    }
    
    /**
     * Construye la URL de la portada del libro.
     * Open Library proporciona portadas en varios tamaños: S, M, L
     */
    private fun buildCoverUrl(isbn: String): String {
        return "$COVER_BASE_URL$isbn$COVER_SIZE_LARGE"
    }
}
