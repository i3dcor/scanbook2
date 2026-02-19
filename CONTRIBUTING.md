# Guía de Contribución - ScanBook

Gracias por contribuir a ScanBook. Esta guía te ayudará a mantener consistencia con el resto del proyecto.

---

## 1. Convención de Nombres de Ramas

### 1.1 Estructura general

```
tipo/descripcion-corta-y-clara
```

**Reglas:**
- Todo en minúsculas
- Palabras separadas por guiones (`-`)
- Sin caracteres especiales ni números de tickets (a menos que sea necesario)
- Máximo 3-4 palabras en la descripción
- Descripción en español (consistente con los commits)

### 1.2 Tipos de ramas

| Tipo | Prefijo | Cuándo usar | Ejemplo |
|------|---------|-------------|---------|
| **Feature** | `feature/` | Nueva funcionalidad, mejora UI | `feature/busqueda-avanzada` |
| **Fix** | `fix/` | Corrección de bugs | `fix/crash-lista-vacia` |
| **Refactor** | `refactor/` | Mejora de código sin cambiar funcionalidad | `refactor/extraer-componentes` |
| **Docs** | `docs/` | Documentación, README, comentarios | `docs/api-repositorios` |
| **Test** | `test/` | Añadir o mejorar tests | `test/viewmodel-tests` |
| **Chore** | `chore/` | Tareas de mantenimiento, deps, CI | `chore/actualizar-dependencias` |
| **Hotfix** | `hotfix/` | Fix urgente para producción | `hotfix/save-book-crash` |

### 1.3 Ejemplos por categoría

#### Documentación (`docs/`)
```
docs/readme-completo
docs/arquitectura-mvvm
docs/estimaciones-proyecto
docs/api-booklookup
docs/contribuir
```

#### Features nuevas (`feature/`)
```
feature/sistema-busqueda
feature/filtros-genero
feature/camara-portada
feature/sync-offline
feature/exportar-csv
feature/tema-claro
feature/animaciones-transicion
```

#### Bugs (`fix/`)
```
fix/duplicado-isbn-scan
fix/perdida-datos-rotacion
fix/teclado-oculta-campos
fix/imagen-no-carga
```

#### Refactorización (`refactor/`)
```
refactor/componentes-reutilizables
refactor/separar-modulos
refactor/inyeccion-dependencias
```

#### Tests (`test/`)
```
test/scanresult-viewmodel
test/room-repository
test/ui-editbook
test/integration-busqueda
```

#### Mantenimiento (`chore/`)
```
chore/gradle-8.2
chore/detekt-setup
chore/ci-github-actions
chore/kotlin-1.9
```

---

## 2. Mensajes de Commit

### 2.1 Formato: Conventional Commits

```
<tipo>(<alcance opcional>): <descripción>

<cuerpo opcional>

<pie opcional>
```

### 2.2 Tipos de commit

| Tipo | Uso | Ejemplo |
|------|-----|---------|
| `feat` | Nueva funcionalidad | `feat: añadir búsqueda por voz` |
| `fix` | Corrección de bug | `fix: corregir crash al guardar libro` |
| `refactor` | Cambio de código sin modificar comportamiento | `refactor: extraer componente BookTextField` |
| `docs` | Cambios en documentación | `docs: actualizar README con setup` |
| `test` | Añadir o corregir tests | `test: añadir tests para ViewModel` |
| `chore` | Tareas de mantenimiento | `chore: actualizar Gradle a 8.2` |
| `style` | Formato, punto y coma, etc. (no cambia lógica) | `style: formatear con ktlint` |
| `perf` | Mejora de rendimiento | `perf: optimizar carga de imágenes` |

### 2.3 Reglas

- **Idioma:** Español para todo el proyecto
- **Descripción en imperativo:** "añadir" no "añadido" ni "añadiendo"
- **Primera línea máximo 72 caracteres**
- **Sin punto final** en la primera línea
- **Cuerpo opcional** explicando el "por qué" si es necesario

### 2.4 Ejemplos buenos

```
feat: añadir diálogo de portada ampliada en EditBookScreen

- La miniatura de portada ahora es clickable
- Al pulsar se abre un Dialog con la imagen ampliada
- El diálogo se cierra tocando fuera o sobre la imagen
```

```
fix: corregir detección de ISBN duplicado

El ViewModel no recibía el repositorio correcto, causando
que no detectara libros ya guardados. Ahora se pasa
RoomIsbnRepository explícitamente desde MainActivity.
```

```
refactor: convertir campo Genre a editable

- Reemplazar GenreDropdownField por BookTextField
- Eliminar composable GenreDropdownField sin uso
```

### 2.5 Ejemplos a evitar

❌ `feat: cambios` (demasiado vago)  
❌ `fix: bug` (no describe qué bug)  
❌ `feat: añadida funcionalidad de búsqueda` (no imperativo)  
❌ `update: archivo.kt` (tipo no estándar)  
❌ `WIP: trabajo en progreso` (no uses WIP en commits)  

---

## 3. Flujo de Trabajo (Workflow)

### 3.1 Crear una nueva rama

```bash
# Asegúrate de estar en main y actualizado
git checkout main
git pull origin main

# Crear rama nueva
git checkout -b feature/nombre-de-la-feature

# O si es un fix
git checkout -b fix/descripcion-del-bug
```

### 3.2 Durante el desarrollo

- Commits pequeños y atómicos
- Cada commit debe compilar y funcionar por sí mismo
- Hacer commits frecuentemente (no acumular cambios)

### 3.3 Subir cambios

```bash
# Primer push (crea la rama en remoto)
git push -u origin feature/nombre-de-la-feature

# Pushes posteriores
git push
```

### 3.4 Crear Pull Request

1. Ve a GitHub y crea PR desde tu rama a `main`
2. Título del PR: mismo formato que el commit (ej: `feat: añadir...`)
3. Descripción: explicar qué cambia y por qué
4. Asegurar que pase CI (si está configurado)
5. Solicitar review si hay más personas en el equipo
6. Mergear usando "Squash and merge" o "Create a merge commit" según prefieras

### 3.5 Después del merge

```bash
# Volver a main y actualizar
git checkout main
git pull origin main

# Limpiar ramas locales mergeadas (opcional)
git branch -d feature/nombre-de-la-feature
```

---

## 4. Estilo de Código

### 4.1 General

- **Idioma:** Kotlin (obviamente)
- **Formato:** Usar formateo automático de Android Studio (Ctrl+Alt+L / Cmd+Option+L)
- **Longitud máxima de línea:** 120 caracteres
- **Indentación:** 4 espacios (configuración por defecto)

### 4.2 Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|------------|---------|
| Clases | PascalCase | `BookRepository`, `ScanResultViewModel` |
| Funciones | camelCase | `lookupByIsbn()`, `onSaveClick()` |
| Variables | camelCase | `uiState`, `scannedIsbn` |
| Constantes | UPPER_SNAKE_CASE | `BASE_URL`, `TIMEOUT_MS` |
| Composables | PascalCase (función) | `EditBookScreen()`, `BookTextField()` |
| Archivos de composables | PascalCase | `EditBookScreen.kt` |
| Paquetes | lowercase | `com.i3dcor.scanbook.data.repository` |

### 4.3 Compose específico

- **Parámetros de Composables:** orden alfabético para modifiers opcionales
- **State hoisting:** Siempre que sea posible, subir el estado
- **Preview:** Añadir `@Preview` para componentes reutilizables

```kotlin
@Composable
fun BookTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    // implementation
}

@Preview(showBackground = true)
@Composable
fun BookTextFieldPreview() {
    ScanBookTheme {
        BookTextField(
            label = "Title",
            value = "Sample Book",
            onValueChange = {}
        )
    }
}
```

### 4.4 Arquitectura

Ver `ARCHITECTURE.md` para reglas detalladas. Resumen:

- **Presentation:** Composables + ViewModels + UiState
- **Domain:** Modelos puros Kotlin + interfaces de repositorios
- **Data:** Implementaciones de repositorios + DTOs + mappers
- **Dependencias:** Presentation → Domain ← Data

### 4.5 Documentación

- **KDoc** para clases y funciones públicas
- **Comentarios inline** solo cuando el código no sea autoexplicativo
- **Evitar comentarios obvios:**

```kotlin
// ❌ Mal
// Incrementa el contador
counter++

// ✅ Bien
// Retry con backoff exponencial para no saturar la API
delay(1000 * (2.0.pow(attempt)))
```

---

## 5. Checklist antes de crear PR

- [ ] El código compila sin errores (`./gradlew build`)
- [ ] Los tests pasan (`./gradlew test`)
- [ ] He revisado mi código yo mismo (self-review)
- [ ] Los mensajes de commit siguen la convención
- [ ] La rama está actualizada con `main` (rebase o merge)
- [ ] He eliminado código comentado o debug prints
- [ ] He añadido Preview si es un componente nuevo
- [ ] La descripción del PR explica el "por qué" de los cambios

---

## 6. Recursos Útiles

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Compose API Guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md)
- [Git Flow vs GitHub Flow](https://www.geeksforgeeks.org/git-flow-vs-github-flow/)

---

## 7. Preguntas Frecuentes

**¿Puedo usar inglés en los commits?**  
No, el proyecto usa español consistentemente para mantener coherencia.

**¿Qué hago si mi feature necesita cambios en múltiples archivos?**  
Está bien, pero asegúrate de que todos los cambios estén relacionados con la misma feature. Si no, dividir en PRs separados.

**¿Debo crear tests para todo?**  
Para features nuevas: idealmente sí. Para fixes: un test que reproduzca el bug. Para refactor: los tests existentes deben seguir pasando.

**¿Puedo usar WIP en el nombre de la rama?**  
Mejor no. Si necesitas guardar trabajo incompleto, usa commits temporales que luego harás squash, o usa el sistema de drafts de GitHub PRs.

---

## 8. Contacto

Si tienes dudas sobre cómo contribuir:
- Abre un issue en GitHub
- Consulta `rules.md` para reglas arquitectónicas
- Consulta `ARCHITECTURE.md` para decisiones técnicas

---

**Nota:** Esta guía es un documento vivo. Si crees que falta algo o hay algo que mejorar, ¡abre un PR!
