package com.example.pokedex.data.mapper

import com.example.pokedex.data.remote.dto.NamedApiResourceDto
import com.example.pokedex.data.remote.dto.PokemonDto
import com.example.pokedex.data.remote.dto.PokemonSpeciesDto
import com.example.pokedex.domain.model.Ability
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.domain.model.Stat

/** Extrae el id de una URL de PokeAPI, p. ej. ".../pokemon/25/" -> 25 */
fun idFromUrl(url: String): Int =
    url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0

fun artworkUrl(id: Int): String =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

fun NamedApiResourceDto.toSummary(): PokemonSummary {
    val id = idFromUrl(url)
    return PokemonSummary(id = id, name = name, imageUrl = artworkUrl(id))
}

fun PokemonDto.toSummary(): PokemonSummary = PokemonSummary(
    id = id,
    name = name,
    imageUrl = sprites?.other?.officialArtwork?.frontDefault ?: artworkUrl(id)
)

fun PokemonDto.toDetail(species: PokemonSpeciesDto?): PokemonDetail {
    val description = species?.flavorTextEntries
        ?.let { entries ->
            entries.firstOrNull { it.language?.name == "es" }
                ?: entries.firstOrNull { it.language?.name == "en" }
        }
        ?.flavorText
        ?.replace('\n', ' ')
        ?.replace('\u000c', ' ')

    val genus = species?.genera?.let { genera ->
        genera.firstOrNull { it.language?.name == "es" }
            ?: genera.firstOrNull { it.language?.name == "en" }
    }?.genus

    return PokemonDetail(
        id = id,
        name = name,
        imageUrl = sprites?.other?.officialArtwork?.frontDefault
            ?: sprites?.frontDefault
            ?: artworkUrl(id),
        heightMeters = (height ?: 0) / 10.0,   // PokeAPI usa decímetros
        weightKg = (weight ?: 0) / 10.0,       // PokeAPI usa hectogramos
        baseExperience = baseExperience ?: 0,
        types = types.orEmpty().sortedBy { it.slot }.mapNotNull { it.type?.name },
        abilities = abilities.orEmpty().mapNotNull { slot ->
            slot.ability?.name?.let { Ability(it, slot.isHidden == true) }
        },
        stats = stats.orEmpty().mapNotNull { s ->
            s.stat?.name?.let { Stat(it, s.baseStat ?: 0) }
        },
        description = description,
        genus = genus
    )
}
