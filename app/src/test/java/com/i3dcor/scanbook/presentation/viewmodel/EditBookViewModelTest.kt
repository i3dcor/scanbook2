package com.i3dcor.scanbook.presentation.viewmodel

import com.i3dcor.scanbook.domain.model.BookNotFoundException
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.BookLookupRepository
import com.i3dcor.scanbook.domain.repository.CoverDownloadScheduler
import com.i3dcor.scanbook.domain.repository.IsbnRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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

@OptIn(ExperimentalCoroutinesApi::class)
class EditBookViewModelTest {

    private lateinit var repository: IsbnRepository
    private lateinit var lookupRepository: BookLookupRepository
    private lateinit var coverScheduler: CoverDownloadScheduler
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        lookupRepository = mockk()
        coverScheduler = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(initialBook: ScannedIsbn? = null) =
        EditBookViewModel(
            initialBook = initialBook,
            repository = repository,
            lookupRepository = lookupRepository,
            coverScheduler = coverScheduler,
            ioDispatcher = testDispatcher
        )

    // ============ INITIAL STATE ============

    @Test
    fun `initial state is empty when no book provided`() {
        val vm = viewModel()

        with(vm.uiState.value) {
            assertEquals("", isbn)
            assertEquals("", title)
            assertEquals("", author)
            assertEquals("", genre)
            assertNull(coverUrl)
            assertNull(coverLocalPath)
            assertFalse(isSearching)
            assertNull(searchError)
        }
    }

    @Test
    fun `initial state maps correctly from existing book`() {
        val book = ScannedIsbn(
            isbn = "9780140328721",
            title = "Fantastic Mr. Fox",
            author = "Roald Dahl",
            genre = "Fiction",
            coverUrl = "https://example.com/cover.jpg",
            coverLocalPath = "/data/covers/9780140328721.jpg"
        )

        val vm = viewModel(book)

        with(vm.uiState.value) {
            assertEquals("9780140328721", isbn)
            assertEquals("Fantastic Mr. Fox", title)
            assertEquals("Roald Dahl", author)
            assertEquals("Fiction", genre)
            assertEquals("https://example.com/cover.jpg", coverUrl)
            assertEquals("/data/covers/9780140328721.jpg", coverLocalPath)
        }
    }

    // ============ FIELD CHANGES ============

    @Test
    fun `onTitleChange updates title in state`() {
        val vm = viewModel()
        vm.onTitleChange("Don Quijote")
        assertEquals("Don Quijote", vm.uiState.value.title)
    }

    @Test
    fun `onAuthorChange updates author in state`() {
        val vm = viewModel()
        vm.onAuthorChange("Cervantes")
        assertEquals("Cervantes", vm.uiState.value.author)
    }

    @Test
    fun `onGenreChange updates genre in state`() {
        val vm = viewModel()
        vm.onGenreChange("Novela")
        assertEquals("Novela", vm.uiState.value.genre)
    }

    @Test
    fun `onIsbnChange updates isbn and clears searchError`() = runTest {
        val vm = viewModel()
        coEvery { lookupRepository.lookupByIsbn(any()) } returns Result.failure(BookNotFoundException("x"))

        vm.onIsbnChange("123")
        assertEquals("123", vm.uiState.value.isbn)
        assertNull(vm.uiState.value.searchError)
    }

    // ============ DEBOUNCED LOOKUP ============

    @Test
    fun `short isbn under 10 chars does not trigger lookup`() = runTest {
        val vm = viewModel()

        vm.onIsbnChange("123456789") // 9 chars

        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSearching)
        coVerify(exactly = 0) { lookupRepository.lookupByIsbn(any()) }
    }

    @Test
    fun `isbn with 10 or more chars triggers lookup after 1 second debounce`() = runTest {
        val isbn = "9780140328721"
        val book = ScannedIsbn(isbn = isbn, title = "Found Book", author = "Author")
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(book)

        val vm = viewModel()
        vm.onIsbnChange(isbn)

        // Before debounce: not searching yet
        assertFalse(vm.uiState.value.isSearching)

        // Advance past the 1s debounce
        advanceTimeBy(1100)
        advanceUntilIdle()

        assertEquals("Found Book", vm.uiState.value.title)
        assertFalse(vm.uiState.value.isSearching)
        coVerify(exactly = 1) { lookupRepository.lookupByIsbn(isbn) }
    }

    @Test
    fun `rapid isbn changes cancel previous lookup and only fire once`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(
            ScannedIsbn(isbn = isbn, title = "Book")
        )

        val vm = viewModel()

        // Rapid typing — each change cancels the previous debounce
        vm.onIsbnChange("978014032")
        advanceTimeBy(500)
        vm.onIsbnChange("9780140328")
        advanceTimeBy(500)
        vm.onIsbnChange(isbn)

        // Now advance past debounce
        advanceTimeBy(1100)
        advanceUntilIdle()

        // Should only call lookup once (for the final value)
        coVerify(exactly = 1) { lookupRepository.lookupByIsbn(any()) }
    }

    // ============ LOOKUP FIELD FILLING ============

    @Test
    fun `lookup fills blank title and author`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(
            ScannedIsbn(isbn = isbn, title = "Remote Title", author = "Remote Author")
        )

        val vm = viewModel()
        vm.onIsbnChange(isbn)
        advanceTimeBy(1100)
        advanceUntilIdle()

        assertEquals("Remote Title", vm.uiState.value.title)
        assertEquals("Remote Author", vm.uiState.value.author)
    }

    @Test
    fun `lookup does not overwrite existing title`() = runTest {
        val isbn = "9780140328721"
        val bookWithTitle = ScannedIsbn(isbn = isbn, title = "Existing Title")
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(
            ScannedIsbn(isbn = isbn, title = "Remote Title", author = "Remote Author")
        )

        val vm = viewModel(bookWithTitle)
        vm.onIsbnChange(isbn)
        advanceTimeBy(1100)
        advanceUntilIdle()

        assertEquals("Existing Title", vm.uiState.value.title)
        assertEquals("Remote Author", vm.uiState.value.author)
    }

    @Test
    fun `lookup sets coverUrl when none exists`() = runTest {
        val isbn = "9780140328721"
        val coverUrl = "https://example.com/cover.jpg"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(
            ScannedIsbn(isbn = isbn, coverUrl = coverUrl)
        )

        val vm = viewModel()
        vm.onIsbnChange(isbn)
        advanceTimeBy(1100)
        advanceUntilIdle()

        assertEquals(coverUrl, vm.uiState.value.coverUrl)
    }

    @Test
    fun `lookup does not overwrite existing coverUrl`() = runTest {
        val isbn = "9780140328721"
        val existingCover = "https://existing.com/cover.jpg"
        val book = ScannedIsbn(isbn = isbn, coverUrl = existingCover)
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.success(
            ScannedIsbn(isbn = isbn, coverUrl = "https://remote.com/other.jpg")
        )

        val vm = viewModel(book)
        vm.onIsbnChange(isbn)
        advanceTimeBy(1100)
        advanceUntilIdle()

        assertEquals(existingCover, vm.uiState.value.coverUrl)
    }

    @Test
    fun `lookup failure sets searchError`() = runTest {
        val isbn = "9780140328721"
        coEvery { lookupRepository.lookupByIsbn(isbn) } returns Result.failure(BookNotFoundException(isbn))

        val vm = viewModel()
        vm.onIsbnChange(isbn)
        advanceTimeBy(1100)
        advanceUntilIdle()

        assertEquals("Libro no encontrado", vm.uiState.value.searchError)
        assertFalse(vm.uiState.value.isSearching)
    }

    // ============ PHOTO CAPTURE ============

    @Test
    fun `onLocalCoverCaptured sets coverLocalPath`() {
        val vm = viewModel()
        vm.onLocalCoverCaptured("/data/covers/test.jpg")
        assertEquals("/data/covers/test.jpg", vm.uiState.value.coverLocalPath)
    }

    @Test
    fun `discardCapturedPhoto clears coverLocalPath`() {
        val vm = viewModel()
        vm.onLocalCoverCaptured("/data/covers/test.jpg")
        vm.discardCapturedPhoto()
        assertNull(vm.uiState.value.coverLocalPath)
    }

    @Test
    fun `onLocalCoverCaptured replaces previous captured path`() {
        val vm = viewModel()
        vm.onLocalCoverCaptured("/data/covers/first.jpg")
        vm.onLocalCoverCaptured("/data/covers/second.jpg")
        assertEquals("/data/covers/second.jpg", vm.uiState.value.coverLocalPath)
    }

    @Test
    fun `discardCapturedPhoto deletes the physical file`() {
        val tempFile = java.io.File.createTempFile("cover_test", ".jpg")
        assertTrue(tempFile.exists())

        val vm = viewModel()
        vm.onLocalCoverCaptured(tempFile.absolutePath)
        vm.discardCapturedPhoto()

        assertFalse(tempFile.exists())
    }

    @Test
    fun `discardCapturedPhoto without prior capture does not throw`() {
        val vm = viewModel()
        vm.discardCapturedPhoto()
        assertNull(vm.uiState.value.coverLocalPath)
    }

    @Test
    fun `discardCapturedPhoto does not clear remote coverUrl`() {
        val book = ScannedIsbn(
            isbn = "9780140328721",
            coverUrl = "https://example.com/cover.jpg"
        )
        val vm = viewModel(book)
        vm.onLocalCoverCaptured("/data/covers/local.jpg")
        vm.discardCapturedPhoto()

        assertEquals("https://example.com/cover.jpg", vm.uiState.value.coverUrl)
        assertNull(vm.uiState.value.coverLocalPath)
    }

    // ============ DELETE COVER ============

    @Test
    fun `deleteLocalCover clears both coverUrl and coverLocalPath`() {
        val book = ScannedIsbn(
            isbn = "9780140328721",
            coverUrl = "https://example.com/cover.jpg",
            coverLocalPath = "/data/covers/9780140328721.jpg"
        )

        val vm = viewModel(book)
        vm.deleteLocalCover()

        assertNull(vm.uiState.value.coverUrl)
        assertNull(vm.uiState.value.coverLocalPath)
    }

    // ============ SAVE ============

    @Test
    fun `onSave inserts into repository and calls onComplete`() = runTest {
        val vm = viewModel()
        vm.onTitleChange("Mi libro")

        var completed = false
        vm.onSave { completed = true }
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.insert(any()) }
        assertTrue(completed)
    }

    @Test
    fun `onSave schedules cover download when coverUrl present and no local path`() = runTest {
        val isbn = "9780140328721"
        val coverUrl = "https://example.com/cover.jpg"
        val book = ScannedIsbn(isbn = isbn, coverUrl = coverUrl)

        val vm = viewModel(book)
        vm.onSave {}
        advanceUntilIdle()

        coVerify(exactly = 1) { coverScheduler.scheduleCoverDownload(isbn, coverUrl) }
    }

    @Test
    fun `onSave skips cover download when coverLocalPath is present`() = runTest {
        val isbn = "9780140328721"
        val book = ScannedIsbn(
            isbn = isbn,
            coverUrl = "https://example.com/cover.jpg",
            coverLocalPath = "/data/covers/$isbn.jpg"
        )

        val vm = viewModel(book)
        vm.onSave {}
        advanceUntilIdle()

        coVerify(exactly = 0) { coverScheduler.scheduleCoverDownload(any(), any()) }
    }

    @Test
    fun `onSave skips cover download when no coverUrl`() = runTest {
        val vm = viewModel()
        vm.onSave {}
        advanceUntilIdle()

        coVerify(exactly = 0) { coverScheduler.scheduleCoverDownload(any(), any()) }
    }
}
