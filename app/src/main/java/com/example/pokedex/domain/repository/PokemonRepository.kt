package com.example.pokedex.domain.repository

import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.model.PokemonPage
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.util.AppResult

/** Abstracción del acceso a datos. La UI y los ViewModel solo conocen esta interfaz. */
interface PokemonRepository {
    suspend fun getPokemonPage(offset: Int, limit: Int): AppResult<PokemonPage>
    suspend fun searchByName(query: String): AppResult<List<PokemonSummary>>
    suspend fun getTypes(): AppResult<List<String>>
    suspend fun getPokemonByType(type: String): AppResult<List<PokemonSummary>>
    suspend fun getPokemonDetail(nameOrId: String): AppResult<PokemonDetail>
}
