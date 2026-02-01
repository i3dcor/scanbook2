# ARCHITECTURE.md

Este documento define las **reglas arquitectónicas obligatorias** del proyecto Android. Claude **debe leer y respetar este fichero antes de realizar cualquier cambio**. Si una petición entra en conflicto con estas normas, debe detenerse y proponer una alternativa compatible.

El objetivo del proyecto es un **MVP rápido**, pero con **buenas prácticas reales**, evitando tanto el código desordenado como la sobreingeniería.

---

## 1. Contexto general

- Tipo de app: **Android**
- Lenguaje: **Kotlin**
- Proyecto: **Nuevo (greenfield)**
- UI: **Jetpack Compose exclusivamente** (no XML, no Views)
- Enfoque: rapidez + mantenibilidad

La arquitectura debe permitir iterar rápido sin hipotecar el futuro del código.

---

## 2. Arquitectura base

Se utiliza **MVVM + Clean Architecture**.

Las capas deben estar **claramente separadas**, con responsabilidades explícitas. No se permite lógica fuera de su capa correspondiente.

**No se modulariza el proyecto**: todo vive en un solo módulo, pero organizado por paquetes.

### Capas definidas

1. **Presentation**
2. **Domain**
3. **Data**

La dirección de dependencias es estricta:

```
Presentation → Domain ← Data
```

- Presentation **depende** de Domain
- Data **depende** de Domain
- Domain **no depende de nadie**

---

## 3. Capa Presentation

Responsable de:
- UI con **Jetpack Compose**
- Gestión de estado
- Interacción con ViewModels

### Reglas

- Toda la UI se implementa con **Compose**
- Cada pantalla tiene su **ViewModel**
- El ViewModel:
  - No conoce Retrofit
  - No conoce DTOs
  - No contiene lógica de infraestructura

### Estado de la UI

- El estado debe representarse de forma explícita (por ejemplo: `UiState`)
- Los errores deben ser **parte del estado**, no side-effects invisibles
- La UI solo reacciona al estado, no ejecuta lógica

---

## 4. Capa Domain

Es el **corazón de la aplicación**.

Responsable de:
- Lógica de negocio
- Casos de uso
- Modelos de dominio
- Interfaces de repositorios

### Reglas

- Código **100% Kotlin puro**
- Sin dependencias de Android
- Sin Retrofit
- Sin anotaciones de framework

### Use Cases

- Cada caso de uso representa una acción clara del dominio
- No se crean use cases innecesarios
- Si una operación no aporta lógica real, puede omitirse el use case

### Modelos de dominio

- Simples
- Enfocados al negocio, no a la API
- No deben reflejar directamente la estructura de red

---

## 5. Capa Data

Responsable de:
- Acceso a red
- Mapeo de datos
- Implementación de repositorios

### Reglas

- Usa **Retrofit** para red
- Contiene:
  - DTOs
  - Mappers
  - Implementaciones de repositorios

### Acceso a red

**Toda llamada a internet debe seguir estas reglas obligatorias**:

1. Verificar conectividad **antes** de la llamada
2. Ejecutar la llamada
3. Capturar errores de red y de servidor
4. Devolver siempre un resultado controlado

No se permite lanzar excepciones crudas hacia arriba.

### Manejo de errores

- Los errores deben mapearse a un modelo entendible por el dominio
- El dominio expone errores que la UI pueda representar
- La UI nunca interpreta excepciones técnicas

Ejemplo conceptual:
- Sin internet
- Timeout
- Error HTTP
- Error desconocido

---

## 6. Repositorios

- Definidos como **interfaces en Domain**
- Implementados en Data

Los ViewModels **solo conocen interfaces**, nunca implementaciones concretas.

---

## 7. Patrones y pragmatismo

### Permitido

- Repository
- Mapper
- Use Case (cuando aporta valor)
- State hoisting en Compose

### Evitar

- Clases genéricas innecesarias
- Abstracciones sin uso claro
- Capas vacías
- "God classes"

La regla principal es:

> Usa patrones solo cuando reducen complejidad real.

---

## 8. Buenas prácticas generales

- Código legible antes que ingenioso
- Nombres claros
- Clases pequeñas
- Funciones con una responsabilidad

No se optimiza prematuramente.

---

## 9. Cambios y refactors

Claude debe:

- Respetar siempre esta arquitectura
- No introducir nuevas capas sin justificación
- No refactorizar masivamente sin motivo
- Minimizar archivos tocados

Si una petición del usuario rompe estas reglas:

- **Detenerse**
- Explicar el conflicto
- Proponer una alternativa compatible

---

## 10. Regla final

Este documento es **normativo**.

Si hay duda entre velocidad y claridad arquitectónica, se prioriza **claridad**.

El MVP debe poder crecer sin tener que reescribirse desde cero.

