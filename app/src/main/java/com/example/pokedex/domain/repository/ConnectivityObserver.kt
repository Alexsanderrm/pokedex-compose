package com.example.pokedex.domain.repository

import kotlinx.coroutines.flow.Flow

/** Emite true cuando hay conexión a internet y false cuando no. */
interface ConnectivityObserver {
    val isOnline: Flow<Boolean>
}
