package com.i3dcor.scanbook.data.repository

import com.i3dcor.scanbook.domain.model.BookNotFoundException
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.BookLookupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CompositeBookLookupRepositoryTest {

    private val isbn = "9780140328721"
    
    private val testBook = ScannedIsbn(
        isbn = isbn,
        title = "Test Book",
        author = "Test Author"
    )

    // ============ SUCCESS CASES ============

    @Test
    fun `lookupByIsbn returns success from first repository`() = runTest {
        // Given
        val firstRepo: BookLookupRepository = mockk()
        val secondRepo: BookLookupRepository = mockk()
        
        coEvery { firstRepo.lookupByIsbn(isbn) } returns Result.success(testBook)
        
        val composite = CompositeBookLookupRepository(listOf(firstRepo, secondRepo))

        // When
        val result = composite.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testBook, result.getOrNull())
        
        // Second repo should NOT be called
        coVerify(exactly = 0) { secondRepo.lookupByIsbn(any()) }
    }

    @Test
    fun `lookupByIsbn falls back to second repository when first returns BookNotFoundException`() = runTest {
        // Given
        val firstRepo: BookLookupRepository = mockk()
        val secondRepo: BookLookupRepository = mockk()
        
        coEvery { firstRepo.lookupByIsbn(isbn) } returns Result.failure(BookNotFoundException(isbn))
        coEvery { secondRepo.lookupByIsbn(isbn) } returns Result.success(testBook)
        
        val composite = CompositeBookLookupRepository(listOf(firstRepo, secondRepo))

        // When
        val result = composite.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testBook, result.getOrNull())
        
        // Both repos should be called
        coVerify(exactly = 1) { firstRepo.lookupByIsbn(isbn) }
        coVerify(exactly = 1) { secondRepo.lookupByIsbn(isbn) }
    }

    // ============ ERROR PROPAGATION ============

    @Test
    fun `lookupByIsbn propagates network error without trying next repository`() = runTest {
        // Given
        val firstRepo: BookLookupRepository = mockk()
        val secondRepo: BookLookupRepository = mockk()
        val networkError = IOException("No internet")
        
        coEvery { firstRepo.lookupByIsbn(isbn) } returns Result.failure(networkError)
        
        val composite = CompositeBookLookupRepository(listOf(firstRepo, secondRepo))

        // When
        val result = composite.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        
        // Second repo should NOT be called for network errors
        coVerify(exactly = 0) { secondRepo.lookupByIsbn(any()) }
    }

    @Test
    fun `lookupByIsbn returns BookNotFoundException when all repositories fail to find book`() = runTest {
        // Given
        val firstRepo: BookLookupRepository = mockk()
        val secondRepo: BookLookupRepository = mockk()
        
        coEvery { firstRepo.lookupByIsbn(isbn) } returns Result.failure(BookNotFoundException(isbn))
        coEvery { secondRepo.lookupByIsbn(isbn) } returns Result.failure(BookNotFoundException(isbn))
        
        val composite = CompositeBookLookupRepository(listOf(firstRepo, secondRepo))

        // When
        val result = composite.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BookNotFoundException)
        
        // Both repos should be called
        coVerify(exactly = 1) { firstRepo.lookupByIsbn(isbn) }
        coVerify(exactly = 1) { secondRepo.lookupByIsbn(isbn) }
    }

    // ============ EDGE CASES ============

    @Test
    fun `lookupByIsbn with single repository works correctly`() = runTest {
        // Given
        val singleRepo: BookLookupRepository = mockk()
        coEvery { singleRepo.lookupByIsbn(isbn) } returns Result.success(testBook)
        
        val composite = CompositeBookLookupRepository(listOf(singleRepo))

        // When
        val result = composite.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testBook, result.getOrNull())
    }

    @Test
    fun `lookupByIsbn with three repositories tries until success`() = runTest {
        // Given
        val firstRepo: BookLookupRepository = mockk()
        val secondRepo: BookLookupRepository = mockk()
        val thirdRepo: BookLookupRepository = mockk()
        
        coEvery { firstRepo.lookupByIsbn(isbn) } returns Result.failure(BookNotFoundException(isbn))
        coEvery { secondRepo.lookupByIsbn(isbn) } returns Result.failure(BookNotFoundException(isbn))
        coEvery { thirdRepo.lookupByIsbn(isbn) } returns Result.success(testBook)
        
        val composite = CompositeBookLookupRepository(listOf(firstRepo, secondRepo, thirdRepo))

        // When
        val result = composite.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testBook, result.getOrNull())
        
        // All three repos should be called in order
        coVerify(exactly = 1) { firstRepo.lookupByIsbn(isbn) }
        coVerify(exactly = 1) { secondRepo.lookupByIsbn(isbn) }
        coVerify(exactly = 1) { thirdRepo.lookupByIsbn(isbn) }
    }

    @Test
    fun `lookupByIsbn returns failure when second repo has network error`() = runTest {
        // Given
        val firstRepo: BookLookupRepository = mockk()
        val secondRepo: BookLookupRepository = mockk()
        val networkError = IOException("Timeout")
        
        coEvery { firstRepo.lookupByIsbn(isbn) } returns Result.failure(BookNotFoundException(isbn))
        coEvery { secondRepo.lookupByIsbn(isbn) } returns Result.failure(networkError)
        
        val composite = CompositeBookLookupRepository(listOf(firstRepo, secondRepo))

        // When
        val result = composite.lookupByIsbn(isbn)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }
}
