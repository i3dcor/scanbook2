package com.i3dcor.scanbook.data.repository

import com.i3dcor.scanbook.data.local.dao.BookDao
import com.i3dcor.scanbook.data.local.mapper.toDomain
import com.i3dcor.scanbook.data.local.mapper.toEntity
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.IsbnRepository

/**
 * Implementación de IsbnRepository con persistencia Room.
 * Métodos síncronos: deben llamarse desde un hilo de background (Dispatchers.IO).
 */
class RoomIsbnRepository(
    private val bookDao: BookDao
) : IsbnRepository {

    override fun insert(scannedIsbn: ScannedIsbn) {
        bookDao.insert(scannedIsbn.toEntity())
    }

    override fun exists(isbn: String): Boolean {
        return bookDao.exists(isbn)
    }

    override fun delete(isbn: String) {
        bookDao.delete(isbn)
    }

    override fun getAll(): List<ScannedIsbn> {
        return bookDao.getAll().map { it.toDomain() }
    }

    override fun getByIsbn(isbn: String): ScannedIsbn? {
        return bookDao.getByIsbn(isbn)?.toDomain()
    }
}
