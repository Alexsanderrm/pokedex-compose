package com.example.pokedex.ui.components

import androidx.compose.ui.graphics.Color

/** "mr-mime" -> "Mr Mime" */
fun String.toDisplayName(): String =
    split('-').joinToString(" ") { part -> part.replaceFirstChar { it.titlecase() } }

fun Int.toPokedexNumber(): String = "#" + toString().padStart(3, '0')

private val typeNamesEs = mapOf(
    "normal" to "Normal", "fighting" to "Lucha", "flying" to "Volador",
    "poison" to "Veneno", "ground" to "Tierra", "rock" to "Roca",
    "bug" to "Bicho", "ghost" to "Fantasma", "steel" to "Acero",
    "fire" to "Fuego", "water" to "Agua", "grass" to "Planta",
    "electric" to "Eléctrico", "psychic" to "Psíquico", "ice" to "Hielo",
    "dragon" to "Dragón", "dark" to "Siniestro", "fairy" to "Hada"
)

fun typeLabel(type: String): String = typeNamesEs[type] ?: type.toDisplayName()

fun typeColor(type: String): Color = when (type) {
    "normal" -> Color(0xFFA8A77A)
    "fire" -> Color(0xFFEE8130)
    "water" -> Color(0xFF6390F0)
    "electric" -> Color(0xFFF7D02C)
    "grass" -> Color(0xFF7AC74C)
    "ice" -> Color(0xFF96D9D6)
    "fighting" -> Color(0xFFC22E28)
    "poison" -> Color(0xFFA33EA1)
    "ground" -> Color(0xFFE2BF65)
    "flying" -> Color(0xFFA98FF3)
    "psychic" -> Color(0xFFF95587)
    "bug" -> Color(0xFFA6B91A)
    "rock" -> Color(0xFFB6A136)
    "ghost" -> Color(0xFF735797)
    "dragon" -> Color(0xFF6F35FC)
    "dark" -> Color(0xFF705746)
    "steel" -> Color(0xFFB7B7CE)
    "fairy" -> Color(0xFFD685AD)
    else -> Color(0xFF9E9E9E)
}

fun statLabel(stat: String): String = when (stat) {
    "hp" -> "PS"
    "attack" -> "Ataque"
    "defense" -> "Defensa"
    "special-attack" -> "At. Esp."
    "special-defense" -> "Def. Esp."
    "speed" -> "Velocidad"
    else -> stat.toDisplayName()
}
