package com.i3dcor.scanbook.presentation.viewmodel

import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.CoverDownloadScheduler
import com.i3dcor.scanbook.domain.repository.IsbnRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var repository: IsbnRepository
    private lateinit var coverScheduler: CoverDownloadScheduler
    private val testDispatcher = StandardTestDispatcher()

    private val book1 = ScannedIsbn(isbn = "1111111111", title = "El Quijote", author = "Cervantes")
    private val book2 = ScannedIsbn(isbn = "2222222222", title = "Cien años de soledad", author = "García Márquez")
    private val book3 = ScannedIsbn(isbn = "3333333333", title = "La Regenta", author = "Clarín")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        coverScheduler = mockk(relaxed = true)
        coEvery { repository.getAll() } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HomeViewModel(repository, coverScheduler, testDispatcher)

    // ============ INITIAL LOAD ============

    @Test
    fun `init loads books from repository`() = runTest {
        coEvery { repository.getAll() } returns listOf(book1, book2)

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(2, vm.books.value.size)
    }

    @Test
    fun `refresh reloads books from repository`() = runTest {
        coEvery { repository.getAll() } returns listOf(book1)

        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(1, vm.books.value.size)

        coEvery { repository.getAll() } returns listOf(book1, book2, book3)
        vm.refresh()
        advanceUntilIdle()

        assertEquals(3, vm.books.value.size)
    }

    // ============ SEARCH FILTERING ============

    @Test
    fun `filteredBooks returns all books when query is blank`() = runTest {
        coEvery { repository.getAll() } returns listOf(book1, book2, book3)

        val vm = viewModel()
        advanceUntilIdle()

        val results = mutableListOf<List<ScannedIsbn>>()
        val job = launch { vm.filteredBooks.collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        assertEquals(3, results.last().size)
    }

    @Test
    fun `filteredBooks filters by title case-insensitive`() = runTest {
        coEvery { repository.getAll() } returns listOf(book1, book2, book3)

        val vm = viewModel()
        advanceUntilIdle()

        val results = mutableListOf<List<ScannedIsbn>>()
        val job = launch { vm.filteredBooks.collect { results.add(it) } }

        vm.onSearchQueryChange("QUIJOTE")
        advanceUntilIdle()
        job.cancel()

        val filtered = results.last()
        assertEquals(1, filtered.size)
        assertEquals("El Quijote", filtered.first().title)
    }

    @Test
    fun `filteredBooks filters by author case-insensitive`() = runTest {
        coEvery { repository.getAll() } returns listOf(book1, book2, book3)

        val vm = viewModel()
        advanceUntilIdle()

        val results = mutableListOf<List<ScannedIsbn>>()
        val job = launch { vm.filteredBooks.collect { results.add(it) } }

        vm.onSearchQueryChange("garcía")
        advanceUntilIdle()
        job.cancel()

        val filtered = results.last()
        assertEquals(1, filtered.size)
        assertEquals("García Márquez", filtered.first().author)
    }

    @Test
    fun `filteredBooks filters by isbn`() = runTest {
        coEvery { repository.getAll() } returns listOf(book1, book2, book3)

        val vm = viewModel()
        advanceUntilIdle()

        val results = mutableListOf<List<ScannedIsbn>>()
        val job = launch { vm.filteredBooks.collect { results.add(it) } }

        vm.onSearchQueryChange("2222")
        advanceUntilIdle()
        job.cancel()

        val filtered = results.last()
        assertEquals(1, filtered.size)
        assertEquals("2222222222", filtered.first().isbn)
    }

    @Test
    fun `filteredBooks returns empty when no match`() = runTest {
        coEvery { repository.getAll() } returns listOf(book1, book2, book3)

        val vm = viewModel()
        advanceUntilIdle()

        val results = mutableListOf<List<ScannedIsbn>>()
        val job = launch { vm.filteredBooks.collect { results.add(it) } }

        vm.onSearchQueryChange("xyz_no_existe")
        advanceUntilIdle()
        job.cancel()

        assertTrue(results.last().isEmpty())
    }

    @Test
    fun `onSearchQueryChange updates searchQuery state`() = runTest {
        val vm = viewModel()
        vm.onSearchQueryChange("quijote")
        assertEquals("quijote", vm.searchQuery.value)
    }

    // ============ ADD / DELETE ============

    @Test
    fun `addBook inserts and refreshes list`() = runTest {
        coEvery { repository.getAll() } returns emptyList()
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(0, vm.books.value.size)

        coEvery { repository.getAll() } returns listOf(book1)
        vm.addBook(book1)
        advanceUntilIdle()

        coVerify { repository.insert(book1) }
        assertEquals(1, vm.books.value.size)
    }

    @Test
    fun `deleteBook removes and refreshes list`() = runTest {
        coEvery { repository.getAll() } returns listOf(book1, book2)
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(2, vm.books.value.size)

        coEvery { repository.getAll() } returns listOf(book2)
        vm.deleteBook(book1.isbn)
        advanceUntilIdle()

        coVerify { repository.delete(book1.isbn) }
        assertEquals(1, vm.books.value.size)
    }

    // ============ PENDING COVERS ============

    @Test
    fun `downloadPendingCovers schedules only books with coverUrl and no local path`() = runTest {
        val bookWithUrl = ScannedIsbn(isbn = "1111", coverUrl = "https://example.com/a.jpg", coverLocalPath = null)
        val bookWithLocal = ScannedIsbn(isbn = "2222", coverUrl = "https://example.com/b.jpg", coverLocalPath = "/data/covers/2222.jpg")
        val bookNoUrl = ScannedIsbn(isbn = "3333", coverUrl = null, coverLocalPath = null)
        coEvery { repository.getAll() } returns listOf(bookWithUrl, bookWithLocal, bookNoUrl)

        val vm = viewModel()
        vm.downloadPendingCovers()
        advanceUntilIdle()

        coVerify(exactly = 1) { coverScheduler.scheduleCoverDownload("1111", "https://example.com/a.jpg") }
        coVerify(exactly = 0) { coverScheduler.scheduleCoverDownload("2222", any()) }
        coVerify(exactly = 0) { coverScheduler.scheduleCoverDownload("3333", any()) }
    }

    @Test
    fun `downloadPendingCovers is no-op when scheduler is null`() = runTest {
        coEvery { repository.getAll() } returns listOf(
            ScannedIsbn(isbn = "1111", coverUrl = "https://example.com/a.jpg")
        )

        val vm = HomeViewModel(repository, coverScheduler = null, ioDispatcher = testDispatcher)
        vm.downloadPendingCovers()
        advanceUntilIdle()

        // No exception thrown, getAll not called for covers
        coVerify(exactly = 0) { coverScheduler.scheduleCoverDownload(any(), any()) }
    }
}
