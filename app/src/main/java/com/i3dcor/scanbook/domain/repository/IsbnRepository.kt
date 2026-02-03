package com.i3dcor.scanbook.domain.repository

import com.i3dcor.scanbook.domain.model.ScannedIsbn

/**
 * Interface del repositorio de ISBNs escaneados.
 * Definida en Domain - la implementación vive en Data.
 */
interface IsbnRepository {
    
    /**
     * Inserta un libro escaneado en la base de datos.
     * Si el ISBN ya existe, actualiza los datos.
     */
    fun insert(scannedIsbn: ScannedIsbn)
    
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
    
    /**
     * Obtiene un libro por su ISBN.
     * Retorna null si no existe.
     */
    fun getByIsbn(isbn: String): ScannedIsbn?
}
