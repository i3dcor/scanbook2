package com.i3dcor.scanbook.data.repository

import com.i3dcor.scanbook.domain.model.ScannedIsbn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InMemoryIsbnRepositoryTest {

    private lateinit var repository: InMemoryIsbnRepository

    @Before
    fun setUp() {
        repository = InMemoryIsbnRepository()
    }

    // ============ INSERT ============

    @Test
    fun insert_addsIsbnToRepository() {
        val isbn = "978-84-376-0494-7"
        val scannedIsbn = ScannedIsbn(isbn = isbn)

        repository.insert(scannedIsbn)

        assertTrue(repository.exists(isbn))
    }

    @Test
    fun insert_duplicateIsbn_updatesExisting() {
        val isbn = "978-84-376-0494-7"
        val scannedIsbn1 = ScannedIsbn(isbn = isbn, title = "Titulo 1")
        val scannedIsbn2 = ScannedIsbn(isbn = isbn, title = "Titulo 2")

        repository.insert(scannedIsbn1)
        repository.insert(scannedIsbn2)

        assertEquals(1, repository.getAll().size)
        assertEquals("Titulo 2", repository.getByIsbn(isbn)?.title)
    }

    @Test
    fun insert_withAllFields_storesAllData() {
        val scannedIsbn = ScannedIsbn(
            isbn = "978-84-376-0494-7",
            title = "Don Quijote",
            author = "Cervantes",
            genre = "Novela",
            price = 19.99,
            condition = "Nuevo"
        )

        repository.insert(scannedIsbn)

        val retrieved = repository.getByIsbn(scannedIsbn.isbn)
        assertEquals(scannedIsbn.title, retrieved?.title)
        assertEquals(scannedIsbn.author, retrieved?.author)
        assertEquals(scannedIsbn.genre, retrieved?.genre)
        assertEquals(scannedIsbn.price, retrieved?.price)
        assertEquals(scannedIsbn.condition, retrieved?.condition)
    }

    // ============ EXISTS ============

    @Test
    fun exists_returnsTrueForExistingIsbn() {
        val isbn = "978-84-376-0494-7"
        repository.insert(ScannedIsbn(isbn = isbn))

        assertTrue(repository.exists(isbn))
    }

    @Test
    fun exists_returnsFalseForNonExistingIsbn() {
        assertFalse(repository.exists("978-00-000-0000-0"))
    }

    // ============ DELETE ============

    @Test
    fun delete_removesIsbnFromRepository() {
        val isbn = "978-84-376-0494-7"
        repository.insert(ScannedIsbn(isbn = isbn))

        repository.delete(isbn)

        assertFalse(repository.exists(isbn))
    }

    @Test
    fun delete_nonExistingIsbn_doesNotThrow() {
        // No debe lanzar excepcion
        repository.delete("978-00-000-0000-0")
    }

    // ============ GET ALL ============

    @Test
    fun getAll_returnsEmptyListInitially() {
        val result = repository.getAll()

        assertTrue(result.isEmpty())
    }

    @Test
    fun getAll_returnsAllInsertedIsbns() {
        val isbn1 = "978-84-376-0494-7"
        val isbn2 = "978-0-13-468599-1"
        val isbn3 = "978-1-4028-9462-6"

        repository.insert(ScannedIsbn(isbn = isbn1))
        repository.insert(ScannedIsbn(isbn = isbn2))
        repository.insert(ScannedIsbn(isbn = isbn3))

        val result = repository.getAll()

        assertEquals(3, result.size)
        assertTrue(result.any { it.isbn == isbn1 })
        assertTrue(result.any { it.isbn == isbn2 })
        assertTrue(result.any { it.isbn == isbn3 })
    }

    // ============ GET BY ISBN ============

    @Test
    fun getByIsbn_returnsScannedIsbnWhenExists() {
        val isbn = "978-84-376-0494-7"
        val scannedIsbn = ScannedIsbn(isbn = isbn, title = "Test Book")
        repository.insert(scannedIsbn)

        val result = repository.getByIsbn(isbn)

        assertEquals(scannedIsbn, result)
    }

    @Test
    fun getByIsbn_returnsNullWhenNotExists() {
        val result = repository.getByIsbn("978-00-000-0000-0")

        assertNull(result)
    }
}
