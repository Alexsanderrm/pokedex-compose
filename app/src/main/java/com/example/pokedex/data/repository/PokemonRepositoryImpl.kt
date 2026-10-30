package com.example.pokedex.data.repository

import com.example.pokedex.data.mapper.idFromUrl
import com.example.pokedex.data.mapper.toDetail
import com.example.pokedex.data.mapper.toSummary
import com.example.pokedex.data.remote.PokeApiService
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.model.PokemonPage
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.domain.repository.PokemonRepository
import com.example.pokedex.util.AppResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApiService
) : PokemonRepository {

    override suspend fun getPokemonPage(offset: Int, limit: Int): AppResult<PokemonPage> =
        safeCall {
            val response = api.getPokemonList(offset = offset, limit = limit)
            PokemonPage(
                items = response.results.map { it.toSummary() },
                hasMore = response.next != null
            )
        }

    override suspend fun searchByName(query: String): AppResult<List<PokemonSummary>> {
        val normalized = query.trim().lowercase().replace(' ', '-')
        return try {
            AppResult.Success(listOf(api.getPokemon(normalized).toSummary()))
        } catch (e: HttpException) {
            // PokeAPI devuelve 404 cuando el nombre no existe: no es un error, es "sin resultados".
            if (e.code() == 404) AppResult.Success(emptyList()) else AppResult.Error(httpMessage(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            AppResult.Error(NO_CONNECTION)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: UNKNOWN)
        }
    }

    override suspend fun getTypes(): AppResult<List<String>> = safeCall {
        api.getTypes().results
            .map { it.name }
            .filterNot { it in EXCLUDED_TYPES }
    }

    override suspend fun getPokemonByType(type: String): AppResult<List<PokemonSummary>> =
        safeCall {
            api.getType(type).pokemon.orEmpty()
                .map { it.pokemon.toSummary() }
                .distinctBy { it.id }
                .sortedBy { it.id }
        }

    override suspend fun getPokemonDetail(nameOrId: String): AppResult<PokemonDetail> =
        safeCall {
            val pokemon = api.getPokemon(nameOrId)
            val speciesId = pokemon.species?.url?.let(::idFromUrl)
            // Si la especie falla, mostramos igualmente el detalle sin descripción.
            val species = speciesId?.let { id ->
                try {
                    api.getPokemonSpecies(id)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }
            }
            pokemon.toDetail(species)
        }

    private suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> =
        try {
            AppResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            AppResult.Error(NO_CONNECTION)
        } catch (e: HttpException) {
            AppResult.Error(httpMessage(e))
        } catch (e: Exception) {
            AppResult.Error(e.message ?: UNKNOWN)
        }

    private fun httpMessage(e: HttpException) =
        "El servidor respondió con un error (${e.code()}). Inténtalo más tarde."

    private companion object {
        const val NO_CONNECTION = "No se pudo conectar con el servidor. Revisa tu conexión a internet."
        const val UNKNOWN = "Ocurrió un error inesperado."
        val EXCLUDED_TYPES = setOf("unknown", "shadow", "stellar")
    }
}
