package com.i3dcor.scanbook.data.local.mapper

import com.i3dcor.scanbook.data.local.entity.BookEntity
import com.i3dcor.scanbook.domain.model.ScannedIsbn

/**
 * Mappers entre BookEntity (Data) y ScannedIsbn (Domain).
 * Funciones de extensión para mantener el código limpio.
 */

fun BookEntity.toDomain(): ScannedIsbn = ScannedIsbn(
    isbn = isbn,
    title = title,
    author = author,
    genre = genre,
    price = price,
    condition = condition,
    coverUrl = coverUrl,
    coverLocalPath = coverLocalPath
)

fun ScannedIsbn.toEntity(): BookEntity = BookEntity(
    isbn = isbn,
    title = title,
    author = author,
    genre = genre,
    price = price,
    condition = condition,
    coverUrl = coverUrl,
    coverLocalPath = coverLocalPath
)
