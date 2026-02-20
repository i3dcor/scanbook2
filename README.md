# ScanBook

Una aplicación Android nativa en Kotlin para gestionar colecciones de libros mediante escaneo de ISBN.

## Descripción

ScanBook permite a los usuarios:
- 📚 Escanear códigos de barras/ISBN de libros usando la cámara
- 🔍 Buscar información automática del libro (título, autor, género, portada)
- ✏️ Editar y completar datos del libro manualmente
- 💰 Registrar precio y estado de conservación
- 🏠 Gestionar una colección personal de libros
- 🔎 Buscar libros por ISBN automáticamente al editar

## Características Implementadas

### ✅ Escaneo de ISBN (ML Kit)
- Detección en tiempo real de códigos de barras usando Google ML Kit
- Soporte para ISBN-10 e ISBN-13
- Válido para códigos EAN-13
- Integración con cámara nativa

### ✅ Búsqueda de Libros (APIs)
- **OpenLibrary API**: Primera fuente de metadatos
- **Google Books API**: Fallback automático si OpenLibrary no encuentra el libro
- Datos obtenidos: título, autor(es), género, URL de portada

### ✅ Gestión de Colección (Room Database)
- Persistencia local con SQLite/Room
- Lista de libros con miniaturas de portada
- Estados de conservación: New, Good, Damaged
- Edición completa de metadatos
- Eliminación con confirmación
- Detección de duplicados al escanear
- **Búsqueda en tiempo real por ISBN, título o autor**

### ✅ Edición de Libros
- Formulario completo con campos editables:
  - ISBN (con búsqueda automática por debounce)
  - Título
  - Autor
  - Género
  - Precio
  - Estado de conservación
- Portada del libro con vista ampliada (click para zoom)
- Búsqueda automática al modificar ISBN

### ✅ UI Moderna (Jetpack Compose)
- Diseño minimalista con tema oscuro
- Estados visuales claros (Loading, Success, Error, Empty)
- Navegación fluida entre pantallas
- Componentes reutilizables
- Soporte para botón atrás de Android

## Arquitectura

```
app/src/main/java/com/i3dcor/scanbook/
├── components/               # Componentes UI reutilizables (Compose)
│   ├── CameraScreen.kt
│   ├── ScanResultScreen.kt
│   ├── EditBookScreen.kt
│   ├── HomeScreen.kt
│   ├── BookListItem.kt
│   ├── BookCoverThumbnail.kt
│   └── ...
│
├── presentation/
│   ├── state/               # Estados de UI (UiState)
│   │   ├── ScanResultUiState.kt
│   │   ├── EditBookUiState.kt
│   │   └── ...
│   └── viewmodel/           # ViewModels (MVVM)
│       ├── ScanResultViewModel.kt
│       ├── EditBookViewModel.kt
│       └── ...
│
├── domain/                  # Lógica de negocio pura (Kotlin)
│   ├── model/
│   │   └── ScannedIsbn.kt
│   └── repository/
│       ├── IsbnRepository.kt
│       └── BookLookupRepository.kt
│
├── data/                    # Implementaciones de repositorios
│   ├── repository/
│   │   ├── RoomIsbnRepository.kt
│   │   ├── CompositeBookLookupRepository.kt
│   │   ├── OpenLibraryBookRepository.kt
│   │   └── GoogleBooksRepository.kt
│   ├── local/              # Room Database
│   │   ├── dao/
│   │   ├── entity/
│   │   └── mapper/
│   └── network/            # Retrofit + DTOs
│       ├── dto/
│       └── RetrofitClient.kt
│
└── MainActivity.kt         # Punto de entrada y navegación
```

**Patrón arquitectónico:** MVVM + Clean Architecture  
**Dirección de dependencias:** Presentation → Domain ← Data

## Flujo de Usuario

1. **Home**: Lista de libros guardados (empty state si no hay libros)
2. **Escanear**: Abre cámara, detecta ISBN automáticamente
3. **Resultado**: Muestra datos del libro encontrado o error
   - Si ya existe: mensaje de "libro duplicado"
   - Si es nuevo: opción de añadir a colección o editar
4. **Editar**: Formulario para modificar/Completar datos
   - Búsqueda automática por ISBN con debounce (1s)
   - Portada clickleable para vista ampliada
5. **Guardar**: Persiste en Room y vuelve a Home

## Screenshots

*(Pendiente de añadir)*

## Tecnologías

- **Lenguaje:** Kotlin 1.9+
- **UI:** Jetpack Compose (Material 3)
- **Arquitectura:** MVVM + Clean Architecture
- **Persistencia:** Room (SQLite)
- **Red:** Retrofit + OkHttp + Kotlinx Serialization
- **Imágenes:** Coil
- **Cámara:** CameraX + ML Kit Barcode Scanning
- **Inyección de dependencias:** Manual (constructor injection)
- **Asincronía:** Kotlin Coroutines + Flow

## Configuración Requerida

### Permisos Android
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
```

El usuario debe otorgar permisos en tiempo de ejecución.

## Compilación

```bash
# Compilar proyecto
./gradlew build

# Instalar en dispositivo
./gradlew installDebug

# Ejecutar tests
./gradlew test
```

## Contribuir

Ver [CONTRIBUTING.md](CONTRIBUTING.md) para:
- Convenciones de nombres de ramas (`feature/`, `fix/`, `docs/`, etc.)
- Formato de mensajes de commit (Conventional Commits)
- Flujo de trabajo y estilo de código
- Checklist antes de crear PR

## Roadmap

### ✅ Completado (Fase 1 - MVP)
- [x] Escaneo de ISBN con cámara
- [x] Búsqueda de metadatos (OpenLibrary + Google Books)
- [x] Persistencia local con Room
- [x] Lista de libros con portadas
- [x] Edición completa de libros
- [x] Detección de duplicados
- [x] Búsqueda automática por ISBN en edición

### 🚧 En progreso / Próximo
- [ ] Captura de portada con cámara/galería
- [ ] Exportar/importar colección

### 🔮 Futuro (Nice to have)
- [ ] Integración de IA (Gemini Vision) para análisis de estado
- [ ] Estimación de estado de conservación (Malo, Bueno, Como Nuevo)
- [ ] Sugerencia de precio en EUR (10% por debajo de la media de mercado)
- [ ] Estadísticas de colección
- [ ] Lista de deseos (wishlist)
- [ ] Tracking de préstamos
- [ ] Speech-to-Text para entrada manual
- [ ] Integración con plataformas de venta (Wallapop, Vinted, etc.)
- [ ] Autenticación de usuario
- [ ] Sincronización offline/online

## Licencia

AFFERO GPL v3

## Autor

Suso Cerqueiro - Modern Android Development Expert

---

**Status**: Fase 1 - MVP Completado ✅ (19 features implementadas)  
**Última actualización:** Febrero 2026
