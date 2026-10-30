package com.example.pokedex.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Recurso con nombre y URL, formato que PokeAPI usa en todas sus listas. */
data class NamedApiResourceDto(
    val name: String,
    val url: String
)

/** Respuesta paginada: GET /pokemon?offset&limit y GET /type */
data class NamedResourceListDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<NamedApiResourceDto>
)

/** GET /pokemon/{nameOrId} */
data class PokemonDto(
    val id: Int,
    val name: String,
    val height: Int?,
    val weight: Int?,
    @SerializedName("base_experience") val baseExperience: Int?,
    val types: List<PokemonTypeSlotDto>?,
    val abilities: List<PokemonAbilitySlotDto>?,
    val stats: List<PokemonStatDto>?,
    val sprites: SpritesDto?,
    val species: NamedApiResourceDto?
)

data class PokemonTypeSlotDto(
    val slot: Int,
    val type: NamedApiResourceDto?
)

data class PokemonAbilitySlotDto(
    val ability: NamedApiResourceDto?,
    @SerializedName("is_hidden") val isHidden: Boolean?
)

data class PokemonStatDto(
    @SerializedName("base_stat") val baseStat: Int?,
    val stat: NamedApiResourceDto?
)

data class SpritesDto(
    @SerializedName("front_default") val frontDefault: String?,
    val other: OtherSpritesDto?
)

data class OtherSpritesDto(
    @SerializedName("official-artwork") val officialArtwork: ArtworkDto?
)

data class ArtworkDto(
    @SerializedName("front_default") val frontDefault: String?
)

/** GET /type/{name} */
data class TypeDetailDto(
    val id: Int,
    val name: String,
    val pokemon: List<TypePokemonDto>?
)

data class TypePokemonDto(
    val slot: Int,
    val pokemon: NamedApiResourceDto
)

/** GET /pokemon-species/{id} */
data class PokemonSpeciesDto(
    val id: Int,
    @SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextDto>?,
    val genera: List<GenusDto>?
)

data class FlavorTextDto(
    @SerializedName("flavor_text") val flavorText: String,
    val language: NamedApiResourceDto?
)

data class GenusDto(
    val genus: String,
    val language: NamedApiResourceDto?
)
