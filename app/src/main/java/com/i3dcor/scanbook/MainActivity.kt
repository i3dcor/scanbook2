package com.i3dcor.scanbook

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.i3dcor.scanbook.components.BookListItem
import com.i3dcor.scanbook.components.CameraScreen
import com.i3dcor.scanbook.components.EditBookScreen
import com.i3dcor.scanbook.components.HomeSearchBar
import com.i3dcor.scanbook.components.ScanBarcodeButton
import com.i3dcor.scanbook.components.ScanResultScreen
import com.i3dcor.scanbook.data.local.ScanBookDatabase
import com.i3dcor.scanbook.data.repository.RoomIsbnRepository
import com.i3dcor.scanbook.domain.model.ScannedIsbn
import com.i3dcor.scanbook.presentation.viewmodel.EditBookViewModel
import com.i3dcor.scanbook.presentation.viewmodel.HomeViewModel
import com.i3dcor.scanbook.presentation.viewmodel.ScanResultViewModel
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

// Estados de la pantalla principal
private sealed class AppScreen {
    data object Home : AppScreen()
    data object Camera : AppScreen()
    data class ScanResult(val isbn: String) : AppScreen()
    data class EditBook(val book: ScannedIsbn?, val from: AppScreen) : AppScreen()
}

@Composable
fun ScanBookApp(modifier: Modifier = Modifier) {
    // Estado para controlar la navegación entre pantallas
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    // Crear el repositorio Room una sola vez
    val context = LocalContext.current
    val repository = remember {
        val db = ScanBookDatabase.getInstance(context)
        RoomIsbnRepository(db.bookDao())
    }

    // ViewModel de Home (persiste entre navegaciones)
    val homeViewModel = remember { HomeViewModel(repository) }

    when (val screen = currentScreen) {
        is AppScreen.Home -> {
            // Refrescar libros cada vez que se vuelve a Home
            homeViewModel.refresh()
            val books by homeViewModel.books.collectAsState()

            HomeScreen(
                books = books,
                modifier = modifier,
                onBookClick = { book -> currentScreen = AppScreen.EditBook(book = book, from = AppScreen.Home) },
                onScanClick = { currentScreen = AppScreen.Camera }
            )
        }
        is AppScreen.Camera -> {
            CameraScreen(
                onBackClick = { currentScreen = AppScreen.Home },
                onManualInputClick = { currentScreen = AppScreen.EditBook(book = null, from = AppScreen.Camera) },
                onIsbnDetected = { isbn ->
                    Log.d("ScanBook", "ISBN detected: $isbn")
                    currentScreen = AppScreen.ScanResult(isbn)
                },
                modifier = modifier
            )
        }
        is AppScreen.ScanResult -> {
            // Crear ViewModel con el ISBN detectado
            // remember con key = isbn para recrear el ViewModel si cambia el ISBN
            val viewModel = remember(screen.isbn) {
                ScanResultViewModel(isbn = screen.isbn)
            }
            val uiState by viewModel.uiState.collectAsState()
            
            ScanResultScreen(
                uiState = uiState,
                onBackClick = { currentScreen = AppScreen.Camera },
                onEditClick = { currentScreen = AppScreen.EditBook(book = uiState.scannedIsbn, from = AppScreen.ScanResult(screen.isbn)) },
                onAddClick = { 
                    /* TODO: Add to collection */
                    currentScreen = AppScreen.Home
                },
                modifier = modifier
            )
        }
        is AppScreen.EditBook -> {
            val viewModel = remember(screen) {
                EditBookViewModel(initialBook = screen.book, repository = repository)
            }
            val editUiState by viewModel.uiState.collectAsState()

            EditBookScreen(
                uiState = editUiState,
                onIsbnChange = viewModel::onIsbnChange,
                onTitleChange = viewModel::onTitleChange,
                onAuthorChange = viewModel::onAuthorChange,
                onGenreChange = viewModel::onGenreChange,
                onPriceChange = viewModel::onPriceChange,
                onConditionChange = viewModel::onConditionChange,
                onSaveClick = {
                    viewModel.onSave { currentScreen = AppScreen.Home }
                },
                onBackClick = { currentScreen = screen.from },
                modifier = modifier
            )
        }
    }
}

@Composable
fun HomeScreen(
    books: List<ScannedIsbn>,
    modifier: Modifier = Modifier,
    onBookClick: (ScannedIsbn) -> Unit,
    onScanClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E)) // Fondo oscuro general
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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

            if (books.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay libros aún.\nEscanea tu primer libro.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(books) { book ->
                        BookListItem(
                            title = book.title.orEmpty(),
                            author = book.author.orEmpty(),
                            onItemClick = { onBookClick(book) },
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

        ScanBarcodeButton(
            onClick = onScanClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanBookAppPreview() {
    ScanBookTheme {
        ScanBookApp()
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleBooks = listOf(
        ScannedIsbn(isbn = "1", title = "Clean Code", author = "Robert C. Martin"),
        ScannedIsbn(isbn = "2", title = "The Pragmatic Programmer", author = "Andrew Hunt, David Thomas"),
        ScannedIsbn(isbn = "3", title = "Domain-Driven Design", author = "Eric Evans"),
        ScannedIsbn(isbn = "4", title = "Refactoring", author = "Martin Fowler")
    )
    ScanBookTheme {
        HomeScreen(books = sampleBooks, onBookClick = {}, onScanClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    ScanBookTheme {
        HomeScreen(books = emptyList(), onBookClick = {}, onScanClick = {})
    }
}
