# Pokédex — Consumo de APIs REST en Android

Aplicación Android desarrollada con **Jetpack Compose** que consume la API pública **PokeAPI** (https://pokeapi.co) usando **Retrofit**, con arquitectura en capas e inyección de dependencias con **Hilt**.

## Funcionalidades

- Lista de Pokémon (número, nombre e imagen) en una cuadrícula.
- **Scroll infinito**: carga páginas de 20 elementos al acercarse al final.
- **Búsqueda por nombre o número** (campo de texto → `GET /pokemon/{nombre}`).
- **Filtro por tipo** (chips → `GET /type` y `GET /type/{tipo}`), también paginado.
- **Pantalla de detalle**: imagen, tipos, descripción, categoría, altura, peso, experiencia base, habilidades y estadísticas (`GET /pokemon/{nombre}` + `GET /pokemon-species/{id}`).
- **Indicador de conexión** online/offline en la barra superior y banner cuando no hay red.
- Manejo de errores de red con mensajes y botón **Reintentar**; reintento automático al recuperar la conexión.

## Endpoints utilizados

Base URL: `https://pokeapi.co/api/v2/` — todos los métodos son **GET** y responden **JSON**.

| # | Endpoint | Parámetros | Uso en la app |
|---|----------|-----------|---------------|
| 1 | `/pokemon?offset={n}&limit={n}` | query: `offset`, `limit` | Lista paginada (scroll infinito) |
| 2 | `/pokemon/{nameOrId}` | path: nombre o número | Búsqueda por nombre y detalle |
| 3 | `/type?limit=100` | query: `limit` | Tipos disponibles para el filtro |
| 4 | `/type/{name}` | path: nombre del tipo | Pokémon de un tipo (filtro) |
| 5 | `/pokemon-species/{id}` | path: id de la especie | Descripción y categoría en el detalle |

## Arquitectura

```
app/src/main/java/com/example/pokedex
├── data
│   ├── remote          → PokeApiService (Retrofit) y DTOs
│   ├── mapper          → DTO → modelos de dominio
│   ├── repository      → PokemonRepositoryImpl (manejo de errores)
│   └── network         → NetworkConnectivityObserver (online/offline)
├── domain
│   ├── model           → PokemonSummary, PokemonDetail, Stat, Ability
│   └── repository      → interfaces PokemonRepository y ConnectivityObserver
├── di                  → módulos de Hilt (Retrofit, OkHttp, repositorios)
├── ui
│   ├── list            → PokemonListScreen + PokemonListViewModel
│   ├── detail          → PokemonDetailScreen + PokemonDetailViewModel
│   ├── components      → indicador de conexión, vistas de error/vacío
│   ├── navigation      → Navigation Compose
│   └── theme
└── util                → AppResult (éxito / error)
```

Flujo: **UI (Compose)** → **ViewModel** (StateFlow) → **Repository** (interfaz en domain) → **Retrofit** → PokeAPI.

## Tecnologías

Kotlin 2.0 · Jetpack Compose (Material 3) · Navigation Compose · Hilt · Retrofit + Gson · OkHttp · Coil · Coroutines/Flow.

## Cómo ejecutar

1. Abrir la carpeta del proyecto en Android Studio (Ladybug o más reciente).
2. Esperar la sincronización de Gradle.
3. Ejecutar en un emulador o dispositivo con Android 7.0 (API 24) o superior.

Para probar el indicador offline: activar el modo avión en el emulador.
