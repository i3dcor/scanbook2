# Rules

Documento central de reglas que Claude debe consultar **siempre** antes de realizar cualquier acción en el proyecto.

---

## Reglas activas

### 1. Leer ARCHITECTURE.md antes de cualquier cambio

**Archivo**: `ARCHITECTURE.md`

**Obligación**: Antes de modificar, crear o eliminar cualquier archivo del proyecto, Claude debe leer `ARCHITECTURE.md` y respetar todas las normas definidas en él.

**Resumen de ARCHITECTURE.md**:
- Arquitectura: MVVM + Clean Architecture (un solo módulo)
- Capas: Presentation -> Domain <- Data
- UI: Solo Jetpack Compose (no XML, no Views)
- Domain: Kotlin puro, sin dependencias de Android
- Data: Retrofit, manejo obligatorio de errores de red
- Repositorios: Interfaces en Domain, implementaciones en Data
- Pragmatismo: Patrones solo cuando reducen complejidad
- Minimizar archivos tocados, no refactorizar sin motivo

**Si una petición entra en conflicto**: Detenerse, explicar el conflicto, proponer alternativa compatible.

---

## Cómo añadir nuevas reglas

Añadir una nueva sección bajo "Reglas activas" con:
- Nombre descriptivo de la regla
- Archivo relacionado (si aplica)
- Descripción de la obligación
- Consecuencias de incumplimiento
