package com.i3dcor.scanbook.data.repository

import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.domain.repository.IsbnRepository

/**
 * Implementación en memoria del repositorio de ISBNs.
 * Los datos se pierden al cerrar la aplicación.
 * 
 * Thread-safe mediante sincronización.
 */
class InMemoryIsbnRepository : IsbnRepository {
    
    private val booksMap = mutableMapOf<String, ScannedIsbn>()
    private val lock = Any()
    
    override fun insert(scannedIsbn: ScannedIsbn) {
        synchronized(lock) {
            booksMap[scannedIsbn.isbn] = scannedIsbn
        }
    }
    
    override fun exists(isbn: String): Boolean {
        synchronized(lock) {
            return booksMap.containsKey(isbn)
        }
    }
    
    override fun delete(isbn: String) {
        synchronized(lock) {
            booksMap.remove(isbn)
        }
    }
    
    override fun getAll(): List<ScannedIsbn> {
        synchronized(lock) {
            return booksMap.values.toList()
        }
    }
    
    override fun getByIsbn(isbn: String): ScannedIsbn? {
        synchronized(lock) {
            return booksMap[isbn]
        }
    }
}
