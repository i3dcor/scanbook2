package com.i3dcor.scanbook.presentation.viewmodel

import app.cash.turbine.test
import com.i3dcor.scanbook.domain.model.BookNotFoundException
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.BookLookupRepository
import com.i3dcor.scanbook.domain.repository.IsbnRepository
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
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class ScanResultViewModelTest {

    private lateinit var lookupRepository: BookLookupRepository
    private lateinit var isbnRepository: IsbnRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        lookupRepository = mockk()
        isbnRepository = mockk()
        coEvery { isbnRepository.getByIsbn(any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(isbn: String) =
        ScanResultViewModel(isbn, lookupRepository, isbnRepository, testDispatcher)

    // ============ INITIAL STATE ============

    @Test
    fun `initial state has correct isbn and isLoading true`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(ScannedIsbn(isbn = isbn))

        val vm = viewModel(isbn)

        val initialState = vm.uiState.value
        assertEquals(isbn, initialState.scannedIsbn.isbn)
        assertTrue(initialState.isLoading)
        assertNull(initialState.error)
        assertFalse(initialState.alreadyExists)
    }

    // ============ SUCCESS CASES ============

    @Test
    fun `lookupByIsbn success updates state with book data`() = runTest {
        val isbn = "9780140328721"
        val book = ScannedIsbn(
            isbn = isbn,
            title = "Fantastic Mr. Fox",
            author = "Roald Dahl",
            genre = "Fiction",
            coverUrl = "https://covers.openlibrary.org/b/isbn/9780140328721-L.jpg"
        )
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(book)

        val vm = viewModel(isbn)

        vm.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val final = awaitItem()
            assertFalse(final.isLoading)
            assertNull(final.error)
            assertFalse(final.alreadyExists)
            assertEquals("Fantastic Mr. Fox", final.scannedIsbn.title)
            assertEquals("Roald Dahl", final.scannedIsbn.author)
            assertEquals("Fiction", final.scannedIsbn.genre)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============ ALREADY EXISTS ============

    @Test
    fun `book already in local repo sets alreadyExists true without calling lookup`() = runTest {
        val isbn = "9780140328721"
        val existing = ScannedIsbn(isbn = isbn, title = "Libro guardado")
        coEvery { isbnRepository.getByIsbn(isbn) } returns existing

        val vm = viewModel(isbn)

        vm.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val final = awaitItem()
            assertFalse(final.isLoading)
            assertTrue(final.alreadyExists)
            assertNull(final.error)
            assertEquals("Libro guardado", final.scannedIsbn.title)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { lookupRepository.lookupByIsbn(any()) }
    }

    // ============ ERROR MESSAGES ============

    @Test
    fun `BookNotFoundException maps to libro no encontrado`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.failure(BookNotFoundException(isbn))

        val vm = viewModel(isbn)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Libro no encontrado", vm.uiState.value.error)
    }

    @Test
    fun `UnknownHostException maps to sin conexion`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.failure(UnknownHostException())

        val vm = viewModel(isbn)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Sin conexión a Internet", vm.uiState.value.error)
    }

    @Test
    fun `SocketTimeoutException maps to tiempo de espera agotado`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.failure(SocketTimeoutException())

        val vm = viewModel(isbn)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Tiempo de espera agotado", vm.uiState.value.error)
    }

    @Test
    fun `generic IOException maps to error al buscar`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.failure(IOException("Network error"))

        val vm = viewModel(isbn)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Error al buscar el libro", vm.uiState.value.error)
        assertEquals(isbn, vm.uiState.value.scannedIsbn.isbn)
    }

    // ============ REPOSITORY INTERACTION ============

    @Test
    fun `init calls lookupRepository with isbn when book not in local repo`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(ScannedIsbn(isbn = isbn))

        viewModel(isbn)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { lookupRepository.lookupByIsbn(isbn) }
    }

    // ============ RETRY ============

    @Test
    fun `retry resets loading state and calls lookupRepository again`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.failure(IOException("Error"))

        val vm = viewModel(isbn)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Error al buscar el libro", vm.uiState.value.error)

        val book = ScannedIsbn(isbn = isbn, title = "Success Book")
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(book)

        vm.retry()

        assertTrue(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Success Book", vm.uiState.value.scannedIsbn.title)
        assertNull(vm.uiState.value.error)
        coVerify(exactly = 2) { lookupRepository.lookupByIsbn(isbn) }
    }

    @Test
    fun `retry after success updates to new data`() = runTest {
        val isbn = "9780140328721"
        val book1 = ScannedIsbn(isbn = isbn, title = "First Title")
        val book2 = ScannedIsbn(isbn = isbn, title = "Updated Title")

        coEvery { lookupRepository.lookupByIsbn(isbn) } returnsMany listOf(
            Result.success(book1),
            Result.success(book2)
        )

        val vm = viewModel(isbn)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("First Title", vm.uiState.value.scannedIsbn.title)

        vm.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Updated Title", vm.uiState.value.scannedIsbn.title)
        coVerify(exactly = 2) { lookupRepository.lookupByIsbn(isbn) }
    }
}
