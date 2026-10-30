package com.example.pokedex.util

/** Resultado de una operación de datos: éxito o error con mensaje para el usuario. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String) : AppResult<Nothing>
}
