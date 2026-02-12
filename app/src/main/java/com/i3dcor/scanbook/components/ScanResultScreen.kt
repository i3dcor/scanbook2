package com.i3dcor.scanbook.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.ui.theme.ScanBookTheme

@Composable
fun ScanResultScreen(
    scannedIsbn: ScannedIsbn,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Manejar botón atrás del sistema Android
    BackHandler {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScanResultHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Container for book details with gradient background effect
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF252528)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BookCoverDisplay()
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    BookTitleAndAuthor(
                        title = scannedIsbn.title ?: "Unknown Title",
                        author = scannedIsbn.author ?: "Unknown Author"
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    BookMetadataRow(
                        isbn = scannedIsbn.isbn,
                        genre = scannedIsbn.genre
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ScanResultActions(
                onEditClick = onEditClick,
                onAddClick = onAddClick
            )
        }
    }
}

@Composable
fun ScanResultHeader() {
    Text(
        text = "Scan Result",
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BookCoverDisplay() {
    // Placeholder for book cover with gradient background
    Box(
        modifier = Modifier
            .size(width = 160.dp, height = 240.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4E4E50),
                        Color(0xFF2C2C2E)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder icon until we have real images
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = "Book Cover",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
    }
}

@Composable
fun BookTitleAndAuthor(
    title: String,
    author: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = author,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Gray
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BookMetadataRow(
    isbn: String,
    genre: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetadataBadge(
                icon = Icons.Default.Tag,
                text = isbn
            )
            if (genre != null) {
                MetadataBadge(
                    icon = Icons.Default.Book,
                    text = genre
                )
            }
        }
    }
}

@Composable
fun MetadataBadge(
    icon: ImageVector,
    text: String
) {
    Surface(
        color = Color(0xFF3A3A3C),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun ScanResultActions(
    onEditClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Edit Details")
        }

        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2962FF)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add to Collection")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanResultScreenPreview() {
    ScanBookTheme {
        ScanResultScreen(
            scannedIsbn = ScannedIsbn(
                isbn = "9780743273565",
                title = "The Great Gatsby",
                author = "F. Scott Fitzgerald",
                genre = "Fiction"
            )
        )
    }
}
