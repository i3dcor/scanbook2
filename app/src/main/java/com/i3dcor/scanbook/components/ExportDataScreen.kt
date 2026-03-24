package com.i3dcor.scanbook.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.i3dcor.scanbook.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.i3dcor.scanbook.data.export.BookSerializer
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.ui.theme.ScanBookTheme
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun ExportDataScreen(
    books: List<ScannedIsbn>,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {},
    onExportClick: () -> Unit = {}
) {
    var selectedFormat by remember { mutableStateOf("ZIP") }
    val context = LocalContext.current

    // Calcular tamaño estimado en función del formato seleccionado
    val estimatedSize = remember(books, selectedFormat) {
        BookSerializer.estimateExportSize(books, selectedFormat)
    }

    // SAF launcher: se recrea al cambiar de formato para actualizar el MIME type
    val saveFileLauncher = key(selectedFormat) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(
                when (selectedFormat) {
                    "CSV" -> "text/csv"
                    "ZIP" -> "application/zip"
                    else -> "application/json"
                }
            )
        ) { uri: Uri? ->
            if (uri != null) {
                when (selectedFormat) {
                    "CSV" -> writeContentToUri(context, uri, BookSerializer.booksToCsv(books))
                    "ZIP" -> writeBooksZipToUri(context, uri, books)
                    else -> writeContentToUri(context, uri, BookSerializer.booksToJson(books))
                }
                Toast.makeText(context, R.string.export_completed, Toast.LENGTH_SHORT).show()
                onExportClick()
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
            ExportHeader(onCloseClick = onCloseClick)

            Spacer(modifier = Modifier.height(24.dp))

            EstimatedSizeBadge(sizeText = estimatedSize)

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle(text = stringResource(R.string.export_format_section))
            
            Spacer(modifier = Modifier.height(8.dp))

            ExportFormatOption(
                title = stringResource(R.string.export_csv_title),
                description = stringResource(R.string.export_csv_desc),
                isSelected = selectedFormat == "CSV",
                onClick = { selectedFormat = "CSV" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExportFormatOption(
                title = stringResource(R.string.export_json_title),
                description = stringResource(R.string.export_json_desc),
                isSelected = selectedFormat == "JSON",
                onClick = { selectedFormat = "JSON" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExportFormatOption(
                title = stringResource(R.string.export_zip_title),
                description = stringResource(R.string.export_zip_desc),
                isSelected = selectedFormat == "ZIP",
                onClick = { selectedFormat = "ZIP" }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle(text = stringResource(R.string.export_destination_section))
            
            Spacer(modifier = Modifier.height(8.dp))

            ExportDestinationToggle(
                onSaveClick = {
                    val extension = when (selectedFormat) {
                        "CSV" -> "csv"; "ZIP" -> "zip"; else -> "json"
                    }
                    saveFileLauncher.launch("scanbook_export.$extension")
                },
                onShareClick = { shareExport(context, books, selectedFormat) }
            )
        }
    }
}

@Composable
fun ExportHeader(
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
                contentDescription = stringResource(R.string.export_close),
                tint = Color.White
            )
        }
        
        Text(
            text = stringResource(R.string.export_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
fun EstimatedSizeBadge(sizeText: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(50),
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.export_size, sizeText),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.LightGray
                )
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ExportFormatOption(
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
fun ExportDestinationToggle(
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit
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
        DestinationOption(
            text = stringResource(R.string.export_save),
            icon = Icons.Default.Folder,
            onClick = onSaveClick,
            modifier = Modifier.weight(1f)
        )
        DestinationOption(
            text = stringResource(R.string.export_share),
            icon = Icons.Default.Share,
            onClick = onShareClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DestinationOption(
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

// ── Escritura a fichero ──────────────────────────────────────────────────────

/**
 * Escribe contenido de texto en un Uri proporcionado por SAF (Storage Access Framework).
 */
private fun writeContentToUri(context: Context, uri: Uri, content: String) {
    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.write(content.toByteArray(Charsets.UTF_8))
    }
}

/**
 * Núcleo de escritura ZIP: books.json + covers/{isbn}.jpg para portadas locales.
 */
private fun writeBooksZipContent(zip: ZipOutputStream, books: List<ScannedIsbn>) {
    val json = BookSerializer.booksToJson(books).toByteArray(Charsets.UTF_8)
    zip.putNextEntry(ZipEntry("books.json"))
    zip.write(json)
    zip.closeEntry()
    books.forEach { book ->
        val path = book.coverLocalPath ?: return@forEach
        val file = File(path)
        if (!file.exists()) return@forEach
        zip.putNextEntry(ZipEntry("covers/${book.isbn}.jpg"))
        file.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }
}

/**
 * Escribe un ZIP autocontenido en el Uri dado (destino Guardar vía SAF).
 */
private fun writeBooksZipToUri(context: Context, uri: Uri, books: List<ScannedIsbn>) {
    context.contentResolver.openOutputStream(uri)?.use { out ->
        ZipOutputStream(out).use { zip -> writeBooksZipContent(zip, books) }
    }
}

/**
 * Escribe el export en un fichero temporal (cacheDir/exports/) y lanza el share sheet.
 * Compatible con todos los formatos: CSV, JSON, ZIP.
 */
private fun shareExport(context: Context, books: List<ScannedIsbn>, format: String) {
    val extension = when (format) { "CSV" -> "csv"; "ZIP" -> "zip"; else -> "json" }
    val mimeType = when (format) { "CSV" -> "text/csv"; "ZIP" -> "application/zip"; else -> "application/json" }

    val exportsDir = File(context.cacheDir, "exports").apply {
        mkdirs()
        listFiles()?.forEach { it.delete() }   // HAL-08: limpiar exports anteriores
    }
    val file = File(exportsDir, "scanbook_export.$extension")

    when (format) {
        "CSV" -> file.writeText(BookSerializer.booksToCsv(books), Charsets.UTF_8)
        "ZIP" -> file.outputStream().use { out ->
            ZipOutputStream(out).use { zip -> writeBooksZipContent(zip, books) }
        }
        else -> file.writeText(BookSerializer.booksToJson(books), Charsets.UTF_8)
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

@Preview(showBackground = true)
@Composable
fun ExportDataScreenPreview() {
    val sampleBooks = listOf(
        ScannedIsbn(isbn = "978-0134685991", title = "Effective Java", author = "Joshua Bloch"),
        ScannedIsbn(isbn = "978-0596009205", title = "Head First Design Patterns", author = "Eric Freeman"),
        ScannedIsbn(isbn = "978-0132350884", title = "Clean Code", author = "Robert C. Martin")
    )
    ScanBookTheme {
        ExportDataScreen(books = sampleBooks)
    }
}
