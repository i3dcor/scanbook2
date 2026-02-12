package com.i3dcor.scanbook.presentation.viewmodel

import app.cash.turbine.test
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.BookLookupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ScanResultViewModelTest {

    private lateinit var repository: BookLookupRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============ INITIAL STATE ============

    @Test
    fun `initial state has correct isbn and isLoading true`() = runTest {
        // Given
        val isbn = "9780140328721"
        coEvery { repository.lookupByIsbn(isbn) } returns Result.success(
            ScannedIsbn(isbn = isbn)
        )

        // When
        val viewModel = ScanResultViewModel(isbn, repository)

        // Then - check initial state before coroutine completes
        val initialState = viewModel.uiState.value
        assertEquals(isbn, initialState.scannedIsbn.isbn)
        assertTrue(initialState.isLoading)
        assertNull(initialState.error)
    }

    // ============ SUCCESS CASES ============

    @Test
    fun `lookupByIsbn success updates state with book data`() = runTest {
        // Given
        val isbn = "9780140328721"
        val book = ScannedIsbn(
            isbn = isbn,
            title = "Fantastic Mr. Fox",
            author = "Roald Dahl",
            genre = "Fiction",
            coverUrl = "https://covers.openlibrary.org/b/isbn/9780140328721-L.jpg"
        )
        coEvery { repository.lookupByIsbn(isbn) } returns Result.success(book)

        // When
        val viewModel = ScanResultViewModel(isbn, repository)

        // Then
        viewModel.uiState.test {
            // Initial state
            val initial = awaitItem()
            assertTrue(initial.isLoading)

            // Advance dispatcher to execute coroutine
            testDispatcher.scheduler.advanceUntilIdle()

            // Final state after lookup
            val final = awaitItem()
            assertFalse(final.isLoading)
            assertNull(final.error)
            assertEquals("Fantastic Mr. Fox", final.scannedIsbn.title)
            assertEquals("Roald Dahl", final.scannedIsbn.author)
            assertEquals("Fiction", final.scannedIsbn.genre)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============ FAILURE CASES ============

    @Test
    fun `lookupByIsbn failure sets error message`() = runTest {
        // Given
        val isbn = "9780140328721"
        coEvery { repository.lookupByIsbn(isbn) } returns Result.failure(IOException("Network error"))

        // When
        val viewModel = ScanResultViewModel(isbn, repository)

        // Then
        viewModel.uiState.test {
            // Initial state
            val initial = awaitItem()
            assertTrue(initial.isLoading)

            // Advance dispatcher
            testDispatcher.scheduler.advanceUntilIdle()

            // Final state with error
            val final = awaitItem()
            assertFalse(final.isLoading)
            assertEquals("Could not fetch book data", final.error)
            // ISBN should still be present
            assertEquals(isbn, final.scannedIsbn.isbn)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============ REPOSITORY INTERACTION ============

    @Test
    fun `init calls repository with isbn`() = runTest {
        // Given
        val isbn = "9780140328721"
        coEvery { repository.lookupByIsbn(isbn) } returns Result.success(
            ScannedIsbn(isbn = isbn)
        )

        // When
        ScanResultViewModel(isbn, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { repository.lookupByIsbn(isbn) }
    }

    // ============ RETRY ============

    @Test
    fun `retry resets loading state and calls repository again`() = runTest {
        // Given
        val isbn = "9780140328721"
        coEvery { repository.lookupByIsbn(isbn) } returns Result.failure(IOException("Error"))

        val viewModel = ScanResultViewModel(isbn, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial failure
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Could not fetch book data", viewModel.uiState.value.error)

        // Now setup success for retry
        val book = ScannedIsbn(isbn = isbn, title = "Success Book")
        coEvery { repository.lookupByIsbn(isbn) } returns Result.success(book)

        // When
        viewModel.retry()

        // Then - should be loading again
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)

        // Advance to complete retry
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify success after retry
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Success Book", viewModel.uiState.value.scannedIsbn.title)
        assertNull(viewModel.uiState.value.error)

        // Verify repository was called twice
        coVerify(exactly = 2) { repository.lookupByIsbn(isbn) }
    }

    @Test
    fun `retry after success calls repository again`() = runTest {
        // Given
        val isbn = "9780140328721"
        val book1 = ScannedIsbn(isbn = isbn, title = "First Title")
        val book2 = ScannedIsbn(isbn = isbn, title = "Updated Title")

        coEvery { repository.lookupByIsbn(isbn) } returnsMany listOf(
            Result.success(book1),
            Result.success(book2)
        )

        val viewModel = ScanResultViewModel(isbn, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify first result
        assertEquals("First Title", viewModel.uiState.value.scannedIsbn.title)

        // When - retry
        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should have updated data
        assertEquals("Updated Title", viewModel.uiState.value.scannedIsbn.title)
        coVerify(exactly = 2) { repository.lookupByIsbn(isbn) }
    }
}
