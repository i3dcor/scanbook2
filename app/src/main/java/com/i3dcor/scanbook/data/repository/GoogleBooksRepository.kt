package com.i3dcor.scanbook.data.repository

import com.i3dcor.scanbook.data.network.GoogleBooksApi
import com.i3dcor.scanbook.data.network.RetrofitClient
import com.i3dcor.scanbook.data.network.dto.GoogleBookItem
import com.i3dcor.scanbook.domain.model.BookNotFoundException
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.BookLookupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación de BookLookupRepository usando Google Books API.
 * Se utiliza como fuente secundaria (fallback) cuando Open Library no encuentra el libro.
 */
class GoogleBooksRepository(
    private val api: GoogleBooksApi = RetrofitClient.googleBooksApi
) : BookLookupRepository {

    override suspend fun lookupByIsbn(isbn: String): Result<ScannedIsbn> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.searchByIsbn("isbn:$isbn")
                
                if (response.totalItems == 0 || response.items.isNullOrEmpty()) {
                    return@withContext Result.failure(BookNotFoundException(isbn))
                }
                
                val book = response.items.first()
                val scannedIsbn = mapToScannedIsbn(isbn, book)
                Result.success(scannedIsbn)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Mapea un GoogleBookItem al modelo de dominio ScannedIsbn.
     */
    private fun mapToScannedIsbn(isbn: String, book: GoogleBookItem): ScannedIsbn {
        val volumeInfo = book.volumeInfo
        return ScannedIsbn(
            isbn = isbn,
            title = volumeInfo.title,
            author = volumeInfo.authors?.joinToString(", "),
            genre = volumeInfo.categories?.firstOrNull(),
            coverUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
        )
    }
}
