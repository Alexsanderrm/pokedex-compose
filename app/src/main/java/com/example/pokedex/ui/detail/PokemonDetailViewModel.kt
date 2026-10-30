package com.example.pokedex.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.repository.ConnectivityObserver
import com.example.pokedex.domain.repository.PokemonRepository
import com.example.pokedex.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val pokemon: PokemonDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PokemonRepository,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    val pokemonName: String = checkNotNull(savedStateHandle[ARG_NAME])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> = connectivityObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        load()
        viewModelScope.launch {
            isOnline.drop(1).filter { it }.collect {
                if (_uiState.value is DetailUiState.Error) load()
            }
        }
    }

    fun load() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.getPokemonDetail(pokemonName)) {
                is AppResult.Success -> DetailUiState.Success(result.data)
                is AppResult.Error -> DetailUiState.Error(result.message)
            }
        }
    }

    companion object {
        const val ARG_NAME = "name"
    }
}
