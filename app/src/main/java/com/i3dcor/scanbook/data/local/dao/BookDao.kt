package com.i3dcor.scanbook.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.i3dcor.scanbook.data.local.entity.BookEntity

/**
 * DAO para operaciones CRUD sobre la tabla de libros.
 * Métodos síncronos: deben ejecutarse fuera del hilo principal.
 */
@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(book: BookEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM books WHERE isbn = :isbn)")
    fun exists(isbn: String): Boolean

    @Query("DELETE FROM books WHERE isbn = :isbn")
    fun delete(isbn: String)

    @Query("SELECT * FROM books")
    fun getAll(): List<BookEntity>

    @Query("SELECT * FROM books WHERE isbn = :isbn LIMIT 1")
    fun getByIsbn(isbn: String): BookEntity?

    @Query("UPDATE books SET coverLocalPath = :localPath WHERE isbn = :isbn")
    fun updateCoverLocalPath(isbn: String, localPath: String)
}
