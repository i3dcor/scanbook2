package com.i3dcor.scanbook.data.repository

import com.i3dcor.scanbook.data.network.GoogleBooksApi
import com.i3dcor.scanbook.data.network.dto.GoogleBookItem
import com.i3dcor.scanbook.data.network.dto.GoogleBooksResponse
import com.i3dcor.scanbook.data.network.dto.GoogleImageLinks
import com.i3dcor.scanbook.data.network.dto.GoogleVolumeInfo
import com.i3dcor.scanbook.domain.model.BookNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class GoogleBooksRepositoryTest {

    private lateinit var api: GoogleBooksApi
    private lateinit var repository: GoogleBooksRepository

    @Before
    fun setUp() {
        api = mockk()
        repository = GoogleBooksRepository(api)
    }

    // ============ SUCCESS CASES ============

    @Test
    fun `lookupByIsbn success returns ScannedIsbn with all fields`() = runTest {
        // Given
        val isbn = "9780140328721"
        val response = GoogleBooksResponse(
            totalItems = 1,
            items = listOf(
                GoogleBookItem(
                    id = "abc123",
                    volumeInfo = GoogleVolumeInfo(
                        title = "Fantastic Mr. Fox",
                        authors = listOf("Roald Dahl"),
                        publisher = "Puffin",
                        publishedDate = "1988",
                        imageLinks = GoogleImageLinks(
                            smallThumbnail = "http://books.google.com/small.jpg",
                            thumbnail = "http://books.google.com/thumb.jpg"
                        ),
                        categories = listOf("Fiction", "Children")
                    )
                )
            )
        )

        coEvery { api.searchByIsbn("isbn:$isbn") } returns response

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        val book = result.getOrNull()!!
        assertEquals(isbn, book.isbn)
        assertEquals("Fantastic Mr. Fox", book.title)
        assertEquals("Roald Dahl", book.author)
        assertEquals("Fiction", book.genre)
        assertEquals("https://books.google.com/thumb.jpg", book.coverUrl)
    }

    @Test
    fun `lookupByIsbn with multiple authors joins them with comma`() = runTest {
        // Given
        val isbn = "9780140328721"
        val response = GoogleBooksResponse(
            totalItems = 1,
            items = listOf(
                GoogleBookItem(
                    id = "abc123",
                    volumeInfo = GoogleVolumeInfo(
                        title = "Coauthored Book",
                        authors = listOf("Author One", "Author Two", "Author Three"),
                        publisher = null,
                        publishedDate = null,
                        imageLinks = null,
                        categories = null
                    )
                )
            )
        )

        coEvery { api.searchByIsbn("isbn:$isbn") } returns response

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Author One, Author Two, Author Three", result.getOrNull()?.author)
    }

    @Test
    fun `lookupByIsbn without authors returns null author`() = runTest {
        // Given
        val isbn = "9780140328721"
        val response = GoogleBooksResponse(
            totalItems = 1,
            items = listOf(
                GoogleBookItem(
                    id = "abc123",
                    volumeInfo = GoogleVolumeInfo(
                        title = "Anonymous Book",
                        authors = null,
                        publisher = null,
                        publishedDate = null,
                        imageLinks = null,
                        categories = null
                    )
                )
            )
        )

        coEvery { api.searchByIsbn("isbn:$isbn") } returns response

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()?.author)
        assertNull(result.getOrNull()?.genre)
        assertNull(result.getOrNull()?.coverUrl)
    }

    @Test
    fun `lookupByIsbn converts http to https in coverUrl`() = runTest {
        // Given
        val isbn = "9780140328721"
        val response = GoogleBooksResponse(
            totalItems = 1,
            items = listOf(
                GoogleBookItem(
                    id = "abc123",
                    volumeInfo = GoogleVolumeInfo(
                        title = "Test",
                        authors = null,
                        publisher = null,
                        publishedDate = null,
                        imageLinks = GoogleImageLinks(
                            smallThumbnail = null,
                            thumbnail = "http://insecure.url/image.jpg"
                        ),
                        categories = null
                    )
                )
            )
        )

        coEvery { api.searchByIsbn("isbn:$isbn") } returns response

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("https://insecure.url/image.jpg", result.getOrNull()?.coverUrl)
    }

    // ============ NOT FOUND CASES ============

    @Test
    fun `lookupByIsbn with zero totalItems returns BookNotFoundException`() = runTest {
        // Given
        val isbn = "0000000000000"
        val response = GoogleBooksResponse(
            totalItems = 0,
            items = null
        )

        coEvery { api.searchByIsbn("isbn:$isbn") } returns response

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BookNotFoundException)
        assertEquals(isbn, (result.exceptionOrNull() as BookNotFoundException).isbn)
    }

    @Test
    fun `lookupByIsbn with empty items list returns BookNotFoundException`() = runTest {
        // Given
        val isbn = "0000000000000"
        val response = GoogleBooksResponse(
            totalItems = 0,
            items = emptyList()
        )

        coEvery { api.searchByIsbn("isbn:$isbn") } returns response

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BookNotFoundException)
    }

    // ============ FAILURE CASES ============

    @Test
    fun `lookupByIsbn when api throws exception returns failure`() = runTest {
        // Given
        val isbn = "9780140328721"
        coEvery { api.searchByIsbn("isbn:$isbn") } throws IOException("Network error")

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    // ============ API CALLS VERIFICATION ============

    @Test
    fun `lookupByIsbn calls api with correct isbn query format`() = runTest {
        // Given
        val isbn = "9780140328721"
        val response = GoogleBooksResponse(totalItems = 0, items = null)

        coEvery { api.searchByIsbn("isbn:$isbn") } returns response

        // When
        repository.lookupByIsbn(isbn)

        // Then
        coVerify(exactly = 1) { api.searchByIsbn("isbn:9780140328721") }
    }
}
