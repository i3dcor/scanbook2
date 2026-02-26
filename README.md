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
- 📥 Descargar portadas en background y verlas sin internet (modo avión)
- 📦 Exportar colección a CSV, JSON o ZIP autocontenido
- 📤 Compartir la exportación directamente con otras apps

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
- Edición completa de metadatos
- Eliminación con confirmación
- Detección de duplicados al escanear
- **Búsqueda en tiempo real por ISBN, título o autor**

### ✅ Edición de Libros
- Formulario con campos editables: ISBN, Título, Autor, Género
- Portada del libro con vista ampliada (click para zoom)
- Captura de portada directamente con la cámara (overlay, sin perder el formulario)
- Eliminación de portada con diálogo de confirmación ("¿Eliminar foto definitivamente?")
- Búsqueda automática al modificar ISBN

### ✅ Portadas locales offline (WorkManager)
- Descarga automática en background al guardar un libro
- Compresión local: 100×150px, JPEG Q60 (~8 KB/portada)
- Almacenadas en `filesDir/covers/{isbn}.jpg` (Internal Storage)
- Visibles sin conexión a internet (modo avión)
- Re-descarga automática de portadas pendientes al arrancar la app

### ✅ Exportación de colección
- **CSV**: texto plano, compatible con Excel y hojas de cálculo
- **JSON**: respaldo estructurado con metadatos completos
- **ZIP**: archivo autocontenido con `books.json` + portadas descargadas
- **Destino Guardar**: SAF (Storage Access Framework) para elegir carpeta
- **Destino Compartir**: share sheet del sistema vía FileProvider
- Estimación de tamaño antes de exportar

### ✅ UI Moderna (Jetpack Compose)
- Diseño minimalista con tema oscuro
- Estados visuales claros (Loading, Success, Error, Empty)
- Navegación fluida entre pantallas
- Componentes reutilizables
- Soporte para botón atrás de Android
- Todos los textos en `res/values/strings.xml` — preparado para múltiples idiomas (i18n)
- Botones de acción unificados (`ActionButton`) — altura 42dp, azul primario, icono + texto

## Arquitectura

```
app/src/main/java/com/i3dcor/scanbook/
├── components/               # Componentes UI reutilizables (Compose)
│   ├── ActionButton.kt
│   ├── CameraScreen.kt
│   ├── PhotoCaptureScreen.kt
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
│       ├── BookLookupRepository.kt
│       └── CoverDownloadScheduler.kt
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
│   ├── scheduler/          # WorkManager schedulers
│   │   └── WorkManagerCoverScheduler.kt
│   ├── worker/             # CoroutineWorkers
│   │   └── DownloadCoverWorker.kt
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
4. **Editar**: Formulario para modificar/completar datos
   - Búsqueda automática por ISBN con debounce (1s)
   - Portada clickeable para vista ampliada
   - Botón "Add Photo": abre cámara como overlay para capturar portada propia
5. **Guardar**: Persiste en Room, encola descarga de portada en background y vuelve a Home
6. **Exportar**: Desde Home → menú → Exportar → elige formato (CSV / JSON / ZIP) y destino (Guardar o Compartir)

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
- **Background tasks:** WorkManager (descarga de portadas en background)
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
- [x] Búsqueda en tiempo real por ISBN, título y autor
- [x] Exportar colección (CSV, JSON, ZIP autocontenido)
- [x] Compartir exportación vía share sheet (FileProvider)
- [x] Portadas locales offline con WorkManager
- [x] Captura de portada con cámara
- [x] Simplificar editor (sin precio ni estado de conservación)
- [x] Textos UI en strings.xml — base para i18n (añadir idioma: crear `res/values-{locale}/strings.xml`)
- [x] Unificación de estilo de botones (`ActionButton` reutilizable)
- [x] Eliminar portada del libro con confirmación
- [x] Tests unitarios — 54 tests, 0 failures (ViewModels ×3 + DownloadCoverWorker)
- [x] Auditoría de seguridad OWASP Mobile Top 10 — HAL-09: minificación y shrink resources activados en release build
- [x] Auditoría de seguridad OWASP Mobile Top 10 — HAL-10: backup_rules.xml y data_extraction_rules.xml excluyen BD y portadas de backups ADB/nube

### 🔜 Próximo (alta prioridad)
- [ ] Auditoría OWASP Top 10 — correcciones pendientes: HAL-01 (path traversal ISBN), HAL-02 (checksum ISBN-13), HAL-03 (validación URL portada), HAL-04 (network security config), HAL-05 (HTTP→HTTPS), HAL-06 (writeTimeout), HAL-07 (logs en DEBUG), HAL-08 (limpiar caché share), HAL-11 (SQLCipher, en evaluación), HAL-12 (File.delete silencioso)

### 🚧 Backlog (media prioridad)

### 🔮 Futuro (nice to have)
- [ ] Opción de menú oscuro/claro/según sistema
- [ ] Permite volver a hacer foto de portada cuando ya existe una
- [ ] Añadir idioma inglés (`res/values-en/strings.xml`) — base ya lista
- [ ] Speech-to-Text para entrada manual (icono de micrófono en el campo de texto)

### Refactorizar a futuro (deuda técnica)  
- [ ] BookField pasar a componentes reutilizables y mejorar aspecto
- [ ] showCoverDialog pasar a componentes reutilizables
- [ ] CI/CD básico (GitHub Actions: build + test + lint)
- [ ] Mejorar la velocidad de respuesta de la exportación:
    - En una coroutine con indicador de carga
    - Escribir en background, muestra spinner, luego lanza el intent

### ❌ Descartado
- Selección de portada desde galería del dispositivo + crop básico
- Integración de IA (Gemini Vision) para análisis de estado
- Estimación automática de estado de conservación
- Sugerencia de precio de mercado (EUR)
- Estadísticas de colección
- Lista de deseos (wishlist)
- Tracking de préstamos
- Autenticación de usuario
- Integración con plataformas de venta (Wallapop, Vinted)
- Sincronización offline/online con cloud

## Licencia

AFFERO GPL v3

## Autor

Suso Cerqueiro - Modern Android Development Expert

---

**Status**: Fase 1 - MVP Completado ✅ (28 features implementadas) | Fase 2 - Seguridad OWASP en curso 🔒
**Última actualización:** Febrero 2026
