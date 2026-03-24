package com.i3dcor.scanbook.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.i3dcor.scanbook.R
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.ui.theme.ScanBookTheme
import org.json.JSONArray
import java.io.File
import java.util.zip.ZipInputStream

@Composable
fun ImportDataScreen(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onBooksImported: (List<ScannedIsbn>) -> Unit = {}
) {
    var selectedFormat by remember { mutableStateOf("JSON") }
    val context = LocalContext.current

    // SAF launcher: se recrea al cambiar de formato (igual que ExportDataScreen)
    val openFileLauncher = key(selectedFormat) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                val books = when (selectedFormat) {
                    "CSV" -> readCsvFromUri(context, uri)
                    "ZIP" -> readZipFromUri(context, uri)
                    else -> readJsonFromUri(context, uri)
                }
                if (books != null) {
                    onBooksImported(books)
                    Toast.makeText(context, R.string.import_completed, Toast.LENGTH_SHORT).show()
                    onImportClick()
                } else {
                    Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImportHeader(onCloseClick = onCloseClick)

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(text = stringResource(R.string.import_format_section))

            Spacer(modifier = Modifier.height(8.dp))

            ImportFormatOption(
                title = stringResource(R.string.import_csv_title),
                description = stringResource(R.string.import_csv_desc),
                isSelected = selectedFormat == "CSV",
                onClick = { selectedFormat = "CSV" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ImportFormatOption(
                title = stringResource(R.string.import_json_title),
                description = stringResource(R.string.import_json_desc),
                isSelected = selectedFormat == "JSON",
                onClick = { selectedFormat = "JSON" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ImportFormatOption(
                title = stringResource(R.string.import_zip_title),
                description = stringResource(R.string.import_zip_desc),
                isSelected = selectedFormat == "ZIP",
                onClick = { selectedFormat = "ZIP" }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle(text = stringResource(R.string.import_source_section))

            Spacer(modifier = Modifier.height(8.dp))

            ImportSourceToggle(
                onOpenClick = { openFileLauncher.launch(arrayOf("*/*")) }
            )
        }
    }
}

@Composable
private fun ImportHeader(
    onCloseClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.import_close),
                tint = Color.White
            )
        }

        Text(
            text = stringResource(R.string.import_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
private fun ImportFormatOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (isSelected) Color(0xFF1E2838) else MaterialTheme.colorScheme.surface

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null, // Handled by parent container
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ImportSourceToggle(
    onOpenClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SourceOption(
            text = stringResource(R.string.import_open),
            icon = Icons.Default.FileOpen,
            onClick = onOpenClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SourceOption(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E2838), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// ── Lectura y parseo de archivos ─────────────────────────────────────────────

/**
 * Lee y parsea un CSV desde el Uri dado.
 * Formato esperado: isbn,title,author,genre,price,condition (con cabecera).
 */
private fun readCsvFromUri(context: Context, uri: Uri): List<ScannedIsbn>? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val lines = stream.bufferedReader(Charsets.UTF_8).readLines()
            if (lines.size < 2) return@use emptyList()
            lines.drop(1) // saltar cabecera
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
    } catch (e: Exception) {
        null
    }
}

/**
 * Parsea una línea CSV respetando campos entrecomillados y comillas escapadas.
 */
private fun parseCsvLine(line: String): List<String> {
    val fields = mutableListOf<String>()
    var i = 0
    while (i <= line.length) {
        if (i == line.length) {
            // Línea termina en coma: campo vacío final
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

/**
 * Lee y parsea un JSON desde el Uri dado.
 * Formato esperado: array de objetos con campos isbn, title, author, genre, price, condition, coverUrl.
 */
private fun readJsonFromUri(context: Context, uri: Uri): List<ScannedIsbn>? {
    return try {
        val content = context.contentResolver.openInputStream(uri)?.use {
            it.bufferedReader(Charsets.UTF_8).readText()
        } ?: return null
        parseJsonBooks(content)
    } catch (e: Exception) {
        null
    }
}

private fun parseJsonBooks(content: String): List<ScannedIsbn>? {
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
 * Lee y descomprime un ZIP desde el Uri dado.
 * Espera books.json en la raíz y portadas en covers/{isbn}.jpg.
 * Las portadas se guardan en filesDir/covers/ y se asigna coverLocalPath.
 */
private fun readZipFromUri(context: Context, uri: Uri): List<ScannedIsbn>? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                val covers = mutableMapOf<String, ByteArray>()
                var jsonContent: String? = null

                var entry = zip.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "books.json" -> {
                            jsonContent = zip.readBytes().toString(Charsets.UTF_8)
                        }
                        entry.name.startsWith("covers/") && entry.name.endsWith(".jpg") -> {
                            val isbn = entry.name.removePrefix("covers/").removeSuffix(".jpg")
                            if (isbn.isNotBlank()) covers[isbn] = zip.readBytes()
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }

                val books = parseJsonBooks(jsonContent ?: return@use null) ?: return@use null

                val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
                books.map { book ->
                    val coverBytes = covers[book.isbn]
                    if (coverBytes != null) {
                        val coverFile = File(coversDir, "${book.isbn}.jpg")
                        coverFile.writeBytes(coverBytes)
                        book.copy(coverLocalPath = coverFile.absolutePath)
                    } else {
                        book
                    }
                }
            }
        }
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true)
@Composable
fun ImportDataScreenPreview() {
    ScanBookTheme {
        ImportDataScreen()
    }
}
