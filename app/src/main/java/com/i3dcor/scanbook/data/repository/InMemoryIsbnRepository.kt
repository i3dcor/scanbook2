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
    
    private val isbnSet = mutableSetOf<String>()
    private val lock = Any()
    
    override fun insert(isbn: String) {
        synchronized(lock) {
            isbnSet.add(isbn)
        }
    }
    
    override fun exists(isbn: String): Boolean {
        synchronized(lock) {
            return isbnSet.contains(isbn)
        }
    }
    
    override fun delete(isbn: String) {
        synchronized(lock) {
            isbnSet.remove(isbn)
        }
    }
    
    override fun getAll(): List<ScannedIsbn> {
        synchronized(lock) {
            return isbnSet.map { ScannedIsbn(it) }
        }
    }
}
