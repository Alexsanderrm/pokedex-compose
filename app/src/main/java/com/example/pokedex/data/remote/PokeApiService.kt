package com.example.pokedex.data.remote

import com.example.pokedex.data.remote.dto.NamedResourceListDto
import com.example.pokedex.data.remote.dto.PokemonDto
import com.example.pokedex.data.remote.dto.PokemonSpeciesDto
import com.example.pokedex.data.remote.dto.TypeDetailDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints de PokeAPI (https://pokeapi.co) consumidos con Retrofit.
 * Todos son GET y responden JSON.
 */
interface PokeApiService {

    /** 1. Lista paginada de Pokémon (scroll infinito). */
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): NamedResourceListDto

    /** 2. Pokémon por nombre o número: búsqueda por nombre y pantalla de detalle. */
    @GET("pokemon/{nameOrId}")
    suspend fun getPokemon(@Path("nameOrId") nameOrId: String): PokemonDto

    /** 3. Lista de tipos disponibles (para los chips de filtro). */
    @GET("type")
    suspend fun getTypes(@Query("limit") limit: Int = 100): NamedResourceListDto

    /** 4. Pokémon que pertenecen a un tipo (filtro por tipo). */
    @GET("type/{name}")
    suspend fun getType(@Path("name") name: String): TypeDetailDto

    /** 5. Especie: descripción y categoría para la pantalla de detalle. */
    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): PokemonSpeciesDto

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
