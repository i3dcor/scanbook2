package com.i3dcor.scanbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.i3dcor.scanbook.components.BookListItem
import com.i3dcor.scanbook.components.HomeSearchBar
import androidx.compose.foundation.layout.Box
import com.i3dcor.scanbook.presentation.camera.CameraScreen
import com.i3dcor.scanbook.ui.theme.ScanBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScanBookTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScanBookApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Para el ejemplo, usamos una data class simple. En la app real, vendría del dominio.
data class Book(val id: String, val title: String, val author: String)

@Composable
fun ScanBookApp(modifier: Modifier = Modifier) {
    // Lista de ejemplo. En una app real, vendría de un ViewModel.
    val sampleBooks = remember {
        listOf(
            Book("1", "Clean Code", "Robert C. Martin"),
            Book("2", "The Pragmatic Programmer", "Andrew Hunt, David Thomas"),
            Book("3", "Domain-Driven Design", "Eric Evans"),
            Book("4", "Refactoring", "Martin Fowler")
        )
    }

    var query by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E)) // Fondo oscuro general
    ) {
        HomeSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { /* Lógica de búsqueda */ },
            onMenuClick = { /* Lógica de menú */ },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            profileAction = {
                IconButton(onClick = { /* Lógica de perfil */ }) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleBooks) { book ->
                BookListItem(
                    title = book.title,
                    author = book.author,
                    onItemClick = { /* Navegar al detalle del libro */ },
                    onMoreActionClick = { /* Mostrar menú contextual */ }
                ) {
                    Icon(//TODO cambiar por portada
                        imageVector = Icons.Default.Book,
                        contentDescription = "Book cover",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanBookAppPreview() {
    ScanBookTheme {
        ScanBookApp()
    }
}
