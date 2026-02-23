package com.i3dcor.scanbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un libro en la tabla "books".
 * Los campos opcionales son nullable para reflejar el modelo de dominio.
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val isbn: String,
    val title: String? = null,
    val author: String? = null,
    val genre: String? = null,
    val price: Double? = null,
    val condition: String? = null,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null
)
