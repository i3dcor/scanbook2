package com.i3dcor.scanbook.data.export

import com.i3dcor.scanbook.domain.model.ScannedIsbn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookSerializerTest {

    private val sampleBook = ScannedIsbn(
        isbn = "978-84-376-0494-7",
        title = "Don Quijote de la Mancha",
        author = "Miguel de Cervantes",
        genre = "Novela",
        price = 19.99,
        condition = "Nuevo",
        coverUrl = "https://example.com/cover.jpg"
    )

    private val bookNullFields = ScannedIsbn(isbn = "978-0-13-468599-1")

    // ── booksToJson ───────────────────────────────────────────────────────────

    @Test
    fun booksToJson_listaVacia_devuelveArrayVacio() {
        val result = BookSerializer.booksToJson(emptyList())
        assertEquals("[\n\n]", result)
    }

    @Test
    fun booksToJson_serializaTodosLosCampos() {
        val result = BookSerializer.booksToJson(listOf(sampleBook))
        assertTrue(result.contains("\"isbn\": \"978-84-376-0494-7\""))
        assertTrue(result.contains("\"title\": \"Don Quijote de la Mancha\""))
        assertTrue(result.contains("\"author\": \"Miguel de Cervantes\""))
        assertTrue(result.contains("\"genre\": \"Novela\""))
        assertTrue(result.contains("\"price\": 19.99"))
        assertTrue(result.contains("\"condition\": \"Nuevo\""))
        assertTrue(result.contains("\"coverUrl\": \"https://example.com/cover.jpg\""))
    }

    @Test
    fun booksToJson_camposNull_serializaComoNull() {
        val result = BookSerializer.booksToJson(listOf(bookNullFields))
        assertTrue(result.contains("\"title\": null"))
        assertTrue(result.contains("\"author\": null"))
        assertTrue(result.contains("\"price\": null"))
        assertTrue(result.contains("\"coverUrl\": null"))
    }

    @Test
    fun booksToJson_escapaComillasEnStrings() {
        val book = ScannedIsbn(isbn = "123", title = "El libro \"especial\"")
        val result = BookSerializer.booksToJson(listOf(book))
        assertTrue(result.contains("\"El libro \\\"especial\\\"\""))
    }

    @Test
    fun booksToJson_escapaBarrasInversas() {
        val book = ScannedIsbn(isbn = "123", title = "Ruta: C:\\books")
        val result = BookSerializer.booksToJson(listOf(book))
        assertTrue(result.contains("C:\\\\books"))
    }

    // ── booksToCsv ────────────────────────────────────────────────────────────

    @Test
    fun booksToCsv_contieneCabeceraCorrecta() {
        val result = BookSerializer.booksToCsv(emptyList())
        assertEquals("isbn,title,author,genre,price,condition", result)
    }

    @Test
    fun booksToCsv_serializaTodosLosCampos() {
        val result = BookSerializer.booksToCsv(listOf(sampleBook))
        val lines = result.lines()
        assertEquals(2, lines.size)
        val dataLine = lines[1]
        assertTrue(dataLine.contains("978-84-376-0494-7"))
        assertTrue(dataLine.contains("Don Quijote de la Mancha"))
        assertTrue(dataLine.contains("19.99"))
    }

    @Test
    fun booksToCsv_escapaCamposConComas() {
        val book = ScannedIsbn(isbn = "123", title = "Hunt, David Thomas")
        val result = BookSerializer.booksToCsv(listOf(book))
        assertTrue(result.contains("\"Hunt, David Thomas\""))
    }

    @Test
    fun booksToCsv_escapaCamposConComillas() {
        val book = ScannedIsbn(isbn = "123", title = "El libro \"especial\"")
        val result = BookSerializer.booksToCsv(listOf(book))
        assertTrue(result.contains("\"El libro \"\"especial\"\"\""))
    }

    @Test
    fun booksToCsv_camposNullQuedanVacios() {
        val result = BookSerializer.booksToCsv(listOf(bookNullFields))
        val dataLine = result.lines()[1]
        // isbn,,,,,  (título, autor, género, precio, condición vacíos)
        assertTrue(dataLine.startsWith("978-0-13-468599-1,,,,"))
    }

    // ── parseJsonBooks ────────────────────────────────────────────────────────

    @Test
    fun parseJsonBooks_arrayVacio_devuelveListaVacia() {
        val result = BookSerializer.parseJsonBooks("[]")
        assertNotNull(result)
        assertEquals(0, result!!.size)
    }

    @Test
    fun parseJsonBooks_parsea_todosLosCampos() {
        val json = """[{"isbn":"978-84-376-0494-7","title":"Don Quijote","author":"Cervantes",
            |"genre":"Novela","price":19.99,"condition":"Nuevo","coverUrl":"https://x.com/c.jpg"}]
        """.trimMargin()
        val result = BookSerializer.parseJsonBooks(json)
        assertNotNull(result)
        val book = result!![0]
        assertEquals("978-84-376-0494-7", book.isbn)
        assertEquals("Don Quijote", book.title)
        assertEquals("Cervantes", book.author)
        assertEquals("Novela", book.genre)
        assertEquals(19.99, book.price)
        assertEquals("Nuevo", book.condition)
        assertEquals("https://x.com/c.jpg", book.coverUrl)
    }

    @Test
    fun parseJsonBooks_camposNullEnJson_devuelveNull() {
        val json = """[{"isbn":"123","title":null,"author":null,"price":null}]"""
        val result = BookSerializer.parseJsonBooks(json)
        assertNotNull(result)
        val book = result!![0]
        assertNull(book.title)
        assertNull(book.author)
        assertNull(book.price)
    }

    @Test
    fun parseJsonBooks_ignoraEntradasSinIsbn() {
        val json = """[{"isbn":"","title":"Sin ISBN"},{"isbn":"123","title":"Con ISBN"}]"""
        val result = BookSerializer.parseJsonBooks(json)
        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals("123", result[0].isbn)
    }

    @Test
    fun parseJsonBooks_jsonInvalido_devuelveNull() {
        val result = BookSerializer.parseJsonBooks("esto no es json")
        assertNull(result)
    }

    @Test
    fun parseJsonBooks_jsonVacio_devuelveNull() {
        val result = BookSerializer.parseJsonBooks("")
        assertNull(result)
    }

    // ── parseCsvLine ──────────────────────────────────────────────────────────

    @Test
    fun parseCsvLine_lineaSimple_parsea3Campos() {
        val result = BookSerializer.parseCsvLine("a,b,c")
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun parseCsvLine_campoEntrecomilladoConComa() {
        val result = BookSerializer.parseCsvLine("\"Hunt, David\",b,c")
        assertEquals(listOf("Hunt, David", "b", "c"), result)
    }

    @Test
    fun parseCsvLine_comillasEscapadas() {
        val result = BookSerializer.parseCsvLine("\"El \"\"especial\"\"\",b")
        assertEquals(listOf("El \"especial\"", "b"), result)
    }

    @Test
    fun parseCsvLine_camposVaciosIntermedios() {
        val result = BookSerializer.parseCsvLine("a,,c")
        assertEquals(listOf("a", "", "c"), result)
    }

    @Test
    fun parseCsvLine_soloUnCampo() {
        val result = BookSerializer.parseCsvLine("isbn-solo")
        assertEquals(listOf("isbn-solo"), result)
    }

    // ── parseCsvBooks ─────────────────────────────────────────────────────────

    @Test
    fun parseCsvBooks_soloConCabecera_devuelveListaVacia() {
        val result = BookSerializer.parseCsvBooks("isbn,title,author,genre,price,condition")
        assertEquals(0, result.size)
    }

    @Test
    fun parseCsvBooks_ignoraFilasConIsbnVacio() {
        val csv = "isbn,title,author,genre,price,condition\n,Sin ISBN,Autor,,,"
        val result = BookSerializer.parseCsvBooks(csv)
        assertEquals(0, result.size)
    }

    // ── Roundtrip JSON ────────────────────────────────────────────────────────

    @Test
    fun roundtrip_json_preservaTodosLosCampos() {
        val books = listOf(sampleBook)
        val json = BookSerializer.booksToJson(books)
        val parsed = BookSerializer.parseJsonBooks(json)
        assertNotNull(parsed)
        val book = parsed!![0]
        assertEquals(sampleBook.isbn, book.isbn)
        assertEquals(sampleBook.title, book.title)
        assertEquals(sampleBook.author, book.author)
        assertEquals(sampleBook.genre, book.genre)
        assertEquals(sampleBook.price, book.price)
        assertEquals(sampleBook.condition, book.condition)
        assertEquals(sampleBook.coverUrl, book.coverUrl)
    }

    @Test
    fun roundtrip_json_camposNullPreservados() {
        val books = listOf(bookNullFields)
        val json = BookSerializer.booksToJson(books)
        val parsed = BookSerializer.parseJsonBooks(json)
        assertNotNull(parsed)
        val book = parsed!![0]
        assertEquals(bookNullFields.isbn, book.isbn)
        assertNull(book.title)
        assertNull(book.author)
        assertNull(book.price)
    }

    @Test
    fun roundtrip_json_variosLibros() {
        val books = listOf(sampleBook, bookNullFields)
        val json = BookSerializer.booksToJson(books)
        val parsed = BookSerializer.parseJsonBooks(json)
        assertNotNull(parsed)
        assertEquals(2, parsed!!.size)
    }

    // ── Roundtrip CSV ─────────────────────────────────────────────────────────

    @Test
    fun roundtrip_csv_preservaCamposSinFotos() {
        val books = listOf(sampleBook)
        val csv = BookSerializer.booksToCsv(books)
        val parsed = BookSerializer.parseCsvBooks(csv)
        assertEquals(1, parsed.size)
        val book = parsed[0]
        assertEquals(sampleBook.isbn, book.isbn)
        assertEquals(sampleBook.title, book.title)
        assertEquals(sampleBook.author, book.author)
        assertEquals(sampleBook.genre, book.genre)
        assertEquals(sampleBook.price, book.price)
        assertEquals(sampleBook.condition, book.condition)
        // coverUrl no se exporta en CSV
        assertNull(book.coverUrl)
    }

    @Test
    fun roundtrip_csv_camposConComas() {
        val book = ScannedIsbn(isbn = "123", author = "Hunt, David Thomas")
        val csv = BookSerializer.booksToCsv(listOf(book))
        val parsed = BookSerializer.parseCsvBooks(csv)
        assertEquals("Hunt, David Thomas", parsed[0].author)
    }

    @Test
    fun roundtrip_csv_camposConComillas() {
        val book = ScannedIsbn(isbn = "123", title = "El \"arte\" de programar")
        val csv = BookSerializer.booksToCsv(listOf(book))
        val parsed = BookSerializer.parseCsvBooks(csv)
        assertEquals("El \"arte\" de programar", parsed[0].title)
    }

    // ── estimateExportSize ────────────────────────────────────────────────────

    @Test
    fun estimateExportSize_listaVacia_devuelveCero() {
        assertEquals("0 B", BookSerializer.estimateExportSize(emptyList(), "CSV"))
        assertEquals("0 B", BookSerializer.estimateExportSize(emptyList(), "JSON"))
        assertEquals("0 B", BookSerializer.estimateExportSize(emptyList(), "ZIP"))
    }

    @Test
    fun estimateExportSize_csvEsMenorQueJson() {
        val books = List(10) { sampleBook }
        val csv = BookSerializer.estimateExportSize(books, "CSV")
        val json = BookSerializer.estimateExportSize(books, "JSON")
        // Extraemos los bytes para comparar
        val csvBytes = csv.removePrefix("~").removeSuffix(" B")
            .removeSuffix(" KB").removeSuffix(" MB").toDouble()
        val jsonBytes = json.removePrefix("~").removeSuffix(" B")
            .removeSuffix(" KB").removeSuffix(" MB").toDouble()
        assertTrue("CSV debería ser menor que JSON", csvBytes < jsonBytes)
    }

    @Test
    fun estimateExportSize_formatoDesconocido_tratadoComoJson() {
        val books = listOf(sampleBook)
        val result = BookSerializer.estimateExportSize(books, "XML")
        assertEquals(BookSerializer.estimateExportSize(books, "JSON"), result)
    }
}
