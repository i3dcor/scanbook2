package com.i3dcor.scanbook.data.repository

import com.i3dcor.scanbook.data.network.OpenLibraryApi
import com.i3dcor.scanbook.data.network.dto.AuthorDto
import com.i3dcor.scanbook.data.network.dto.AuthorRef
import com.i3dcor.scanbook.data.network.dto.OpenLibraryBookDto
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

class OpenLibraryBookRepositoryTest {

    private lateinit var api: OpenLibraryApi
    private lateinit var repository: OpenLibraryBookRepository

    @Before
    fun setUp() {
        api = mockk()
        repository = OpenLibraryBookRepository(api)
    }

    // ============ SUCCESS CASES ============

    @Test
    fun `lookupByIsbn success returns ScannedIsbn with all fields`() = runTest {
        // Given
        val isbn = "9780140328721"
        val bookDto = OpenLibraryBookDto(
            title = "Fantastic Mr. Fox",
            authors = listOf(AuthorRef("/authors/OL34184A")),
            publishers = listOf("Puffin"),
            publishDate = "1988",
            covers = listOf(123),
            subjects = listOf("Fiction", "Animals")
        )
        val authorDto = AuthorDto(name = "Roald Dahl", personalName = null)

        coEvery { api.getBookByIsbn(isbn) } returns bookDto
        coEvery { api.getAuthor("authors/OL34184A") } returns authorDto

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        val book = result.getOrNull()!!
        assertEquals(isbn, book.isbn)
        assertEquals("Fantastic Mr. Fox", book.title)
        assertEquals("Roald Dahl", book.author)
        assertEquals("Fiction", book.genre)
        assertEquals("https://covers.openlibrary.org/b/isbn/9780140328721-L.jpg", book.coverUrl)
    }

    @Test
    fun `lookupByIsbn success without authors returns ScannedIsbn with null author`() = runTest {
        // Given
        val isbn = "9780140328721"
        val bookDto = OpenLibraryBookDto(
            title = "Anonymous Book",
            authors = null,
            publishers = listOf("Unknown"),
            publishDate = null,
            covers = null,
            subjects = null
        )

        coEvery { api.getBookByIsbn(isbn) } returns bookDto

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        val book = result.getOrNull()!!
        assertEquals("Anonymous Book", book.title)
        assertNull(book.author)
        assertNull(book.genre)
    }

    @Test
    fun `lookupByIsbn success with empty authors list returns null author`() = runTest {
        // Given
        val isbn = "9780140328721"
        val bookDto = OpenLibraryBookDto(
            title = "Book Without Author",
            authors = emptyList(),
            publishers = null,
            publishDate = null,
            covers = null,
            subjects = listOf("Science")
        )

        coEvery { api.getBookByIsbn(isbn) } returns bookDto

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        val book = result.getOrNull()!!
        assertNull(book.author)
        assertEquals("Science", book.genre)
    }

    @Test
    fun `lookupByIsbn when author fetch fails returns book with null author`() = runTest {
        // Given
        val isbn = "9780140328721"
        val bookDto = OpenLibraryBookDto(
            title = "Fantastic Mr. Fox",
            authors = listOf(AuthorRef("/authors/OL34184A")),
            publishers = listOf("Puffin"),
            publishDate = "1988",
            covers = listOf(123),
            subjects = listOf("Fiction")
        )

        coEvery { api.getBookByIsbn(isbn) } returns bookDto
        coEvery { api.getAuthor("authors/OL34184A") } throws IOException("Network error")

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        val book = result.getOrNull()!!
        assertEquals("Fantastic Mr. Fox", book.title)
        assertNull(book.author) // Author fetch failed but book is still returned
    }

    @Test
    fun `lookupByIsbn uses personalName when name is null`() = runTest {
        // Given
        val isbn = "9780140328721"
        val bookDto = OpenLibraryBookDto(
            title = "Test Book",
            authors = listOf(AuthorRef("/authors/OL12345")),
            publishers = null,
            publishDate = null,
            covers = null,
            subjects = null
        )
        val authorDto = AuthorDto(name = null, personalName = "John Doe")

        coEvery { api.getBookByIsbn(isbn) } returns bookDto
        coEvery { api.getAuthor("authors/OL12345") } returns authorDto

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("John Doe", result.getOrNull()?.author)
    }

    // ============ FAILURE CASES ============

    @Test
    fun `lookupByIsbn when api throws exception returns failure`() = runTest {
        // Given
        val isbn = "9780140328721"
        coEvery { api.getBookByIsbn(isbn) } throws IOException("Network error")

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    // ============ COVER URL ============

    @Test
    fun `lookupByIsbn builds cover URL correctly`() = runTest {
        // Given
        val isbn = "9781234567890"
        val bookDto = OpenLibraryBookDto(
            title = "Test",
            authors = null,
            publishers = null,
            publishDate = null,
            covers = null,
            subjects = null
        )

        coEvery { api.getBookByIsbn(isbn) } returns bookDto

        // When
        val result = repository.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            "https://covers.openlibrary.org/b/isbn/9781234567890-L.jpg",
            result.getOrNull()?.coverUrl
        )
    }

    // ============ API CALLS VERIFICATION ============

    @Test
    fun `lookupByIsbn calls api with correct isbn`() = runTest {
        // Given
        val isbn = "9780140328721"
        val bookDto = OpenLibraryBookDto(
            title = "Test",
            authors = null,
            publishers = null,
            publishDate = null,
            covers = null,
            subjects = null
        )

        coEvery { api.getBookByIsbn(isbn) } returns bookDto

        // When
        repository.lookupByIsbn(isbn)

        // Then
        coVerify(exactly = 1) { api.getBookByIsbn(isbn) }
    }

    @Test
    fun `lookupByIsbn calls author api with cleaned key`() = runTest {
        // Given
        val isbn = "9780140328721"
        val bookDto = OpenLibraryBookDto(
            title = "Test",
            authors = listOf(AuthorRef("/authors/OL34184A")),
            publishers = null,
            publishDate = null,
            covers = null,
            subjects = null
        )
        val authorDto = AuthorDto(name = "Author Name", personalName = null)

        coEvery { api.getBookByIsbn(isbn) } returns bookDto
        coEvery { api.getAuthor("authors/OL34184A") } returns authorDto

        // When
        repository.lookupByIsbn(isbn)

        // Then
        // Verifica que se quita el "/" inicial del key
        coVerify(exactly = 1) { api.getAuthor("authors/OL34184A") }
    }
}
