package com.example.pokedex.domain.model

/** Información básica que se muestra en la lista. */
data class PokemonSummary(
    val id: Int,
    val name: String,
    val imageUrl: String
)

/** Una página de resultados del listado general. */
data class PokemonPage(
    val items: List<PokemonSummary>,
    val hasMore: Boolean
)

/** Información completa para la pantalla de detalle. */
data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val heightMeters: Double,
    val weightKg: Double,
    val baseExperience: Int,
    val types: List<String>,
    val abilities: List<Ability>,
    val stats: List<Stat>,
    val description: String?,
    val genus: String?
)

data class Ability(val name: String, val isHidden: Boolean)

data class Stat(val name: String, val value: Int)
