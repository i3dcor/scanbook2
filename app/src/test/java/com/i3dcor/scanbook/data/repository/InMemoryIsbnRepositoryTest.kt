package com.i3dcor.scanbook.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

        repository.insert(isbn)

        assertTrue(repository.exists(isbn))
    }

    @Test
    fun insert_duplicateIsbn_doesNotAddTwice() {
        val isbn = "978-84-376-0494-7"

        repository.insert(isbn)
        repository.insert(isbn)

        assertEquals(1, repository.getAll().size)
    }

    // ============ EXISTS ============

    @Test
    fun exists_returnsTrueForExistingIsbn() {
        val isbn = "978-84-376-0494-7"
        repository.insert(isbn)

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
        repository.insert(isbn)

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

        repository.insert(isbn1)
        repository.insert(isbn2)
        repository.insert(isbn3)

        val result = repository.getAll()

        assertEquals(3, result.size)
        assertTrue(result.any { it.isbn == isbn1 })
        assertTrue(result.any { it.isbn == isbn2 })
        assertTrue(result.any { it.isbn == isbn3 })
    }
}
