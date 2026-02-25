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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.i3dcor.scanbook.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import com.i3dcor.scanbook.presentation.state.EditBookUiState
import com.i3dcor.scanbook.ui.theme.ScanBookTheme

@Composable
fun EditBookScreen(
    uiState: EditBookUiState,
    onIsbnChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    onLocalCoverCaptured: (String) -> Unit,
    onDiscardPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPhotoCapture by remember { mutableStateOf(false) }

    // Manejar botón atrás del sistema Android
    BackHandler {
        onDiscardPhoto()
        onBackClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EditBookHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            BookPhotoSection(
                coverUrl = uiState.coverUrl,
                coverLocalPath = uiState.coverLocalPath,
                onPhotoClick = { showPhotoCapture = true },
                onDeletePhotoClick = { /* Funcionalidad de borrar foto pendiente */ }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Form Fields
            BookTextField(
                label = stringResource(R.string.field_isbn),
                value = uiState.isbn,
                onValueChange = onIsbnChange,
                trailingIcon = {
                    if (uiState.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF448AFF),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
            
            if (uiState.searchError != null) {
                Text(
                    text = uiState.searchError,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFEF5350)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BookTextField(
                label = stringResource(R.string.field_title),
                value = uiState.title,
                onValueChange = onTitleChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BookTextField(
                label = stringResource(R.string.field_author),
                value = uiState.author,
                onValueChange = onAuthorChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BookTextField(
                label = stringResource(R.string.field_genre),
                value = uiState.genre,
                onValueChange = onGenreChange
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            SaveButton(onClick = onSaveClick)

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showPhotoCapture) {
            PhotoCaptureScreen(
                isbn = uiState.isbn,
                onPhotoCaptured = { path ->
                    onLocalCoverCaptured(path)
                    showPhotoCapture = false
                },
                onBackClick = { showPhotoCapture = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun EditBookHeader() {
    Text(
        text = stringResource(R.string.edit_book_title),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BookPhotoSection(
    coverUrl: String? = null,
    coverLocalPath: String? = null,
    onPhotoClick: () -> Unit = {},
    onDeletePhotoClick: () -> Unit = {}
) {
    var showCoverDialog by remember { mutableStateOf(false) }

    if (coverLocalPath != null || coverUrl != null) {
        Surface(
            color = Color(0xFF252528),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(150.dp)
                .aspectRatio(1f)
                .clickable { showCoverDialog = true }
                .border(
                    width = 1.dp,
                    color = Color(0xFF3A3A3C),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            BookCoverThumbnail(
                coverUrl = coverUrl,
                coverLocalPath = coverLocalPath,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showCoverDialog) {
            Dialog(onDismissRequest = { showCoverDialog = false }) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    BookCoverThumbnail(
                        coverUrl = coverUrl,
                        coverLocalPath = coverLocalPath,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(0.65f)
                            .clickable { showCoverDialog = false }
                    )

                    // Badge superpuesto en la esquina superior derecha del Dialog
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3A3A3C)) // Fondo oscuro del badge
                            .border(1.dp, Color(0xFF1C1C1E), CircleShape) // Borde para separación visual
                            .clickable { 
                                onDeletePhotoClick()
                                showCoverDialog = false 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    } else {
        PhotoPlaceholderButton(
            text = stringResource(R.string.front_cover),
            modifier = Modifier.fillMaxWidth(),
            onClick = onPhotoClick
        )
    }
}

@Composable
fun PhotoPlaceholderButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        color = Color(0xFF252528),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = Color(0xFF3A3A3C), // Dark gray border
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF1E2838), CircleShape), // Dark blue circle
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color(0xFF448AFF), // Blue icon
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.add_photo),
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun BookTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Surface(
            color = Color(0xFF252528),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF3A3A3C), RoundedCornerShape(8.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
    }
}

@Composable
fun SaveButton(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        ActionButton(
            text = stringResource(R.string.save_changes),
            icon = Icons.Default.Save,
            onClick = onClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditBookScreenPreview() {
    ScanBookTheme {
        EditBookScreen(
            uiState = EditBookUiState(
                isbn = "978-0-321-12521-7",
                title = "Domain-Driven Design",
                author = "Eric Evans",
                genre = "Computer Science",
                price = "54.99",
                condition = "Good",
                coverUrl = "https://example.com/cover.jpg" // Añadido para forzar que se vea la foto y poder abrir el dialog en preview
            ),
            onIsbnChange = {},
            onTitleChange = {},
            onAuthorChange = {},
            onGenreChange = {},
            onSaveClick = {},
            onBackClick = {},
            onLocalCoverCaptured = {},
            onDiscardPhoto = {}
        )
    }
}
