package com.i3dcor.scanbook.components

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
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.i3dcor.scanbook.ui.theme.ScanBookTheme

@Composable
fun ExportDataScreen(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {},
    onExportClick: () -> Unit = {}
) {
    // State holders for preview purposes
    var selectedFormat by remember { mutableStateOf("JSON") }
    var selectedDestination by remember { mutableStateOf("Guardar") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExportHeader(onCloseClick = onCloseClick)

            Spacer(modifier = Modifier.height(24.dp))

            EstimatedSizeBadge(sizeText = "~24 MB")

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle(text = "FORMATO DE ARCHIVO")
            
            Spacer(modifier = Modifier.height(8.dp))

            ExportFormatOption(
                title = "CSV (Sin fotos)",
                description = "Texto ligero, compatible con Excel y hojas de cálculo.",
                isSelected = selectedFormat == "CSV",
                onClick = { selectedFormat = "CSV" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExportFormatOption(
                title = "JSON (Con fotos)",
                description = "Respaldo completo estructurado con imágenes y metadatos.",
                isSelected = selectedFormat == "JSON",
                onClick = { selectedFormat = "JSON" }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle(text = "DESTINO")
            
            Spacer(modifier = Modifier.height(8.dp))

            ExportDestinationToggle(
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                ExportActionButton(
                    onClick = onExportClick
                )
            }
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
                contentDescription = "Close",
                tint = Color.White
            )
        }
        
        Text(
            text = "Exportar Datos",
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
        color = Color(0xFF252528),
        shape = RoundedCornerShape(50),
        modifier = Modifier.border(1.dp, Color(0xFF3A3A3C), RoundedCornerShape(50))
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
                text = "Tamaño Estimado: $sizeText",
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
    val borderColor = if (isSelected) Color(0xFF2962FF) else Color(0xFF3A3A3C)
    val backgroundColor = if (isSelected) Color(0xFF1E2838) else Color(0xFF252528)

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
                    selectedColor = Color(0xFF2962FF),
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
    selectedDestination: String,
    onDestinationSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF252528), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF3A3A3C), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DestinationOption(
            text = "Guardar",
            icon = Icons.Default.Folder,
            isSelected = selectedDestination == "Guardar",
            onClick = { onDestinationSelected("Guardar") },
            modifier = Modifier.weight(1f)
        )
        
        DestinationOption(
            text = "Compartir",
            icon = Icons.Default.Share,
            isSelected = selectedDestination == "Compartir",
            onClick = { onDestinationSelected("Compartir") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DestinationOption(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF1E2838) else Color.Transparent
    val contentColor = if (isSelected) Color(0xFF2962FF) else Color.Gray

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = contentColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun ExportActionButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2962FF)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(50.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Upload,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Exportar",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExportDataScreenPreview() {
    ScanBookTheme {
        ExportDataScreen()
    }
}
