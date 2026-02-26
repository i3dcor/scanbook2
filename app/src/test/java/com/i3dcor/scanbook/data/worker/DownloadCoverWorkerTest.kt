package com.i3dcor.scanbook.data.worker

import androidx.work.ListenableWorker.Result
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * Tests unitarios para la lógica de descarga de portadas.
 * Se testea DownloadCoverService directamente (extraída de DownloadCoverWorker)
 * para evitar dependencias de WorkManager y Android en tests JVM.
 */
class DownloadCoverWorkerTest {

    private lateinit var mockProcessor: CoverImageProcessor
    private val updaterCalls = mutableListOf<Pair<String, String>>()
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        mockProcessor = mockk()
        tempDir = Files.createTempDirectory("covers_test").toFile()
        updaterCalls.clear()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun service() = DownloadCoverService(
        imageProcessor = mockProcessor,
        pathUpdater = { isbn, path -> updaterCalls.add(isbn to path) }
    )

    // ============ SUCCESS ============

    @Test
    fun `processDownload returns success when processor succeeds`() {
        justRun { mockProcessor.downloadScaleAndSave(any(), any()) }

        val result = service().processDownload("9780140328721", "https://example.com/cover.jpg", tempDir)

        assertEquals(Result.success(), result)
    }

    @Test
    fun `processDownload calls processor with correct url and file`() {
        val isbn = "9780140328721"
        val url = "https://example.com/cover.jpg"
        justRun { mockProcessor.downloadScaleAndSave(any(), any()) }

        service().processDownload(isbn, url, tempDir)

        verify { mockProcessor.downloadScaleAndSave(url, File(tempDir, "$isbn.jpg")) }
    }

    @Test
    fun `processDownload calls pathUpdater with correct isbn`() {
        val isbn = "9780140328721"
        justRun { mockProcessor.downloadScaleAndSave(any(), any()) }

        service().processDownload(isbn, "https://example.com/cover.jpg", tempDir)

        assertEquals(1, updaterCalls.size)
        assertEquals(isbn, updaterCalls.first().first)
    }

    @Test
    fun `processDownload calls pathUpdater with path ending in isbn dot jpg`() {
        val isbn = "9780140328721"
        justRun { mockProcessor.downloadScaleAndSave(any(), any()) }

        service().processDownload(isbn, "https://example.com/cover.jpg", tempDir)

        assertTrue(updaterCalls.first().second.endsWith("$isbn.jpg"))
    }

    // ============ FAILURE / RETRY ============

    @Test
    fun `processDownload returns retry when processor throws IOException`() {
        every { mockProcessor.downloadScaleAndSave(any(), any()) } throws IOException("Network error")

        val result = service().processDownload("9780140328721", "https://example.com/cover.jpg", tempDir)

        assertEquals(Result.retry(), result)
    }

    @Test
    fun `processDownload returns retry on unexpected exception`() {
        every { mockProcessor.downloadScaleAndSave(any(), any()) } throws RuntimeException("Unexpected")

        val result = service().processDownload("isbn", "url", tempDir)

        assertEquals(Result.retry(), result)
    }

    @Test
    fun `processDownload does not call pathUpdater when processor fails`() {
        every { mockProcessor.downloadScaleAndSave(any(), any()) } throws IOException("fail")

        service().processDownload("isbn", "url", tempDir)

        assertTrue(updaterCalls.isEmpty())
    }
}
