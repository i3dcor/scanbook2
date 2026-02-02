package com.i3dcor.scanbook.domain.repository

import com.i3dcor.scanbook.domain.model.ScannedIsbn

/**
 * Interface del repositorio de ISBNs escaneados.
 * Definida en Domain - la implementación vive en Data.
 */
interface IsbnRepository {
    
    /**
     * Inserta un ISBN en la base de datos.
     * Si ya existe, no hace nada (sin duplicados).
     */
    fun insert(isbn: String)
    
    /**
     * Verifica si un ISBN ya existe en la base de datos.
     */
    fun exists(isbn: String): Boolean
    
    /**
     * Elimina un ISBN de la base de datos.
     */
    fun delete(isbn: String)
    
    /**
     * Obtiene todos los ISBNs almacenados.
     */
    fun getAll(): List<ScannedIsbn>
}
