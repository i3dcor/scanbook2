# ScanBook

Una aplicación Android nativa en Kotlin para revender libros usados de forma rápida y sencilla.

## Descripción

ScanBook permite a los usuarios:
- Escanear códigos de barras/ISBN de libros
- Obtener información automática del libro (título, autor, categoría)
- Capturar fotos de la portada y contraportada
- Analizar el estado de conservación con IA (Google Gemini Vision)
- Recibir un precio sugerido para la venta

## Características Principales

### 1. Escaneo Automático (ML Kit)
- Detección instantánea de ISBN usando Google ML Kit
- Congelamiento de cámara y vibración al detectar
- Análisis de códigos EAN-13, UPC, CODE-128, etc.

### 2. (TODO) Integración de IA Generativa (Gemini Vision)
- `AnalyzeBookConditionUseCase`: Analiza fotos de portada y contraportada
- Estimación de estado de conservación (Malo, Bueno, Como Nuevo)
- Sugerencia de precio en EUR (10% por debajo de la media de mercado)
- Respuesta estructurada en JSON

### 3. (TODO) API de Libros
- Google Books API para traer metadatos automáticamente
- Título, autor, idioma, categoría, cover

### 4. UI Moderna (Jetpack Compose + Material 3)
- Diseño minimalista y rápido
- Estados visuales claros (Idle, Scanning, Loading, Success, Error)
- Animaciones fluidas

## Arquitectura

```
app/
├── presentation/              # UI Layer (Jetpack Compose)
│   ├── ui/
│   │   ├── screens/          # ScannerScreen, ResultScreen, etc.
│   │   └── components/       # CameraPreview, etc.
│   └── viewmodel/            # ScanViewModel (StateFlow-based)
│
```

## Flujo de Usuario

1. **Inicio**: Usuario toca "Iniciar Escaneo"
2. **Cámara Always-On**: Se inicia la cámara
3. **Detección ISBN**: ML Kit detecta código, cámara se congela
4. **Fetch Info**: Se consulta Google Books API
5. **Captura Fotos**: Usuario captura portada y contraportada
6. **IA Analysis**: Gemini Vision analiza las imágenes
7. **Resultado**: Se muestra estado y precio sugerido

## Configuración Requerida

### 1. Google Gemini API Key
```bash
export GOOGLE_GEMINI_API_KEY="tu_api_key_aqui"
```

### 2. Permisos Android
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

El usuario debe otorgar permisos en tiempo de ejecución (runtime permissions).

## Compilación

```bash
./gradlew build
```

## Instalación

```bash
./gradlew installDebug
```

## Desarrollo

### Hot Reload (Compose)
Supported natively in Android Studio with Compose tooling.

### Testing
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest   # Instrumentation tests
```

## Consideraciones Futuras (Fase 2+)

- [ ] Persistencia de datos (Room Database)
- [ ] Historial de escaneos
- [ ] Speech-to-Text para entrada manual
- [ ] Entrada manual de ISBN (teclado)
- [ ] Integración con plataformas de venta (Wallapop, Vinted, etc.)
- [ ] Autenticación de usuario
- [ ] Cloud sync
- [ ] Offline support

## Notas de Desarrollo en BlendOS

- Gradle está configurado como estándar (sin dependencias nativas del host)
- CameraX maneja automáticamente las particularidades de hardware
- ML Kit se descarga dinámicamente
- Gemini API funciona sobre HTTPS estándar

## Licencia

MIT

## Autor

Staff Android Engineer - Modern Android Development Expert

---

**Status**: Phase 1 - Skeleton + Scanner Screen Implementation ✅
