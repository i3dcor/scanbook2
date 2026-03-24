package com.i3dcor.scanbook.data.export

import com.i3dcor.scanbook.domain.model.ScannedIsbn
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serialización y deserialización de libros a/desde CSV y JSON.
 * Kotlin puro, sin dependencias de Android.
 * Extraído aquí para ser testeable con tests JVM.
 */
internal object BookSerializer {

    // ── Serialización ─────────────────────────────────────────────────────

    /**
     * Convierte la lista de libros a formato JSON.
     * Serialización manual para evitar dependencias externas (Gson/Moshi).
     */
    fun booksToJson(books: List<ScannedIsbn>): String {
        val items = books.joinToString(",\n") { book ->
            val fields = mutableListOf<String>()
            fields.add("    \"isbn\": ${jsonString(book.isbn)}")
            fields.add("    \"title\": ${jsonString(book.title)}")
            fields.add("    \"author\": ${jsonString(book.author)}")
            fields.add("    \"genre\": ${jsonString(book.genre)}")
            fields.add("    \"price\": ${book.price ?: "null"}")
            fields.add("    \"condition\": ${jsonString(book.condition)}")
            fields.add("    \"coverUrl\": ${jsonString(book.coverUrl)}")
            "  {\n${fields.joinToString(",\n")}\n  }"
        }
        return "[\n$items\n]"
    }

    /**
     * Convierte la lista de libros a formato CSV.
     * Escapa campos que contengan comas, comillas o saltos de línea.
     */
    fun booksToCsv(books: List<ScannedIsbn>): String {
        val header = "isbn,title,author,genre,price,condition"
        val rows = books.joinToString("\n") { book ->
            listOf(
                book.isbn,
                book.title.orEmpty(),
                book.author.orEmpty(),
                book.genre.orEmpty(),
                book.price?.toString().orEmpty(),
                book.condition.orEmpty()
            ).joinToString(",") { field -> escapeCsvField(field) }
        }
        return if (books.isEmpty()) header else "$header\n$rows"
    }

    // ── Deserialización ───────────────────────────────────────────────────

    /**
     * Parsea un string JSON (array) y devuelve la lista de libros.
     * Devuelve null si el JSON no es válido.
     */
    fun parseJsonBooks(content: String): List<ScannedIsbn>? {
        return try {
            val array = JSONArray(content)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.getJSONObject(i)
                val isbn = obj.optString("isbn").trim()
                if (isbn.isBlank()) return@mapNotNull null
                ScannedIsbn(
                    isbn = isbn,
                    title = obj.optString("title").takeIf { it.isNotBlank() },
                    author = obj.optString("author").takeIf { it.isNotBlank() },
                    genre = obj.optString("genre").takeIf { it.isNotBlank() },
                    price = if (obj.isNull("price")) null else obj.optDouble("price").takeIf { !it.isNaN() },
                    condition = obj.optString("condition").takeIf { it.isNotBlank() },
                    coverUrl = obj.optString("coverUrl").takeIf { it.isNotBlank() }
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parsea el contenido completo de un CSV (cabecera + filas) y devuelve los libros.
     * Ignora filas con ISBN vacío.
     */
    fun parseCsvBooks(csvContent: String): List<ScannedIsbn> {
        val lines = csvContent.lines()
        if (lines.size < 2) return emptyList()
        return lines.drop(1) // saltar cabecera
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val fields = parseCsvLine(line)
                val isbn = fields.getOrNull(0).orEmpty().trim()
                if (isbn.isBlank()) return@mapNotNull null
                ScannedIsbn(
                    isbn = isbn,
                    title = fields.getOrNull(1)?.takeIf { it.isNotBlank() },
                    author = fields.getOrNull(2)?.takeIf { it.isNotBlank() },
                    genre = fields.getOrNull(3)?.takeIf { it.isNotBlank() },
                    price = fields.getOrNull(4)?.toDoubleOrNull(),
                    condition = fields.getOrNull(5)?.takeIf { it.isNotBlank() }
                )
            }
    }

    /**
     * Parsea una línea CSV respetando campos entrecomillados y comillas escapadas.
     */
    fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        var i = 0
        while (i <= line.length) {
            if (i == line.length) {
                if (fields.isNotEmpty()) fields.add("")
                break
            }
            if (line[i] == '"') {
                val sb = StringBuilder()
                i++ // saltar comilla inicial
                while (i < line.length) {
                    if (line[i] == '"') {
                        if (i + 1 < line.length && line[i + 1] == '"') {
                            sb.append('"'); i += 2
                        } else {
                            i++; break // comilla de cierre
                        }
                    } else {
                        sb.append(line[i]); i++
                    }
                }
                fields.add(sb.toString())
                if (i < line.length && line[i] == ',') i++
            } else {
                val end = line.indexOf(',', i)
                if (end == -1) {
                    fields.add(line.substring(i)); break
                } else {
                    fields.add(line.substring(i, end)); i = end + 1
                }
            }
        }
        return fields
    }

    // ── Tamaño estimado ───────────────────────────────────────────────────

    /**
     * Estima el tamaño de exportación según la lista de libros y el formato seleccionado.
     */
    fun estimateExportSize(books: List<ScannedIsbn>, format: String): String {
        if (books.isEmpty()) return "0 B"
        val totalBytes = when (format) {
            "CSV" -> 50L + (books.size * 120L)
            "ZIP" -> {
                val jsonBytes = 20L + (books.size * 350L)
                val booksWithLocalCover = books.count { it.coverLocalPath != null }.toLong()
                (jsonBytes * 0.4 + booksWithLocalCover * 8192).toLong()
            }
            else -> 20L + (books.size * 350L) // JSON
        }
        return formatBytes(totalBytes)
    }

    // ── Helpers privados ──────────────────────────────────────────────────

    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    private fun jsonString(value: String?): String {
        if (value == null) return "null"
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "~$bytes B"
            bytes < 1024 * 1024 -> "~%.1f KB".format(bytes / 1024.0)
            else -> "~%.1f MB".format(bytes / (1024.0 * 1024.0))
        }
    }
}
