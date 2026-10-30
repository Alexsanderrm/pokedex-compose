package com.example.pokedex.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.domain.repository.ConnectivityObserver
import com.example.pokedex.domain.repository.PokemonRepository
import com.example.pokedex.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Origen de los datos que se muestran en la lista. */
enum class ListMode {
    ALL,   // GET /pokemon?offset&limit  (paginación en servidor)
    TYPE,  // GET /type/{name}           (paginación local sobre el resultado)
    NAME   // GET /pokemon/{name}        (búsqueda por nombre o número)
}

data class PokemonListUiState(
    val items: List<PokemonSummary> = emptyList(),
    val mode: ListMode = ListMode.ALL,
    val query: String = "",
    val types: List<String> = emptyList(),
    val selectedType: String? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
    val lastSearchedName: String = ""
)

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonListUiState())
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> = connectivityObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** Resultado completo del filtro por tipo; se entrega a la UI por páginas. */
    private var typeResults: List<PokemonSummary> = emptyList()
    private var loadJob: Job? = null

    init {
        loadTypes()
        loadNextPage()
        // Al recuperar la conexión, reintenta automáticamente lo que falló.
        viewModelScope.launch {
            isOnline.drop(1).filter { it }.collect {
                if (_uiState.value.types.isEmpty()) loadTypes()
                if (_uiState.value.error != null) retry()
            }
        }
    }

    /** Lo llama la UI cuando el usuario se acerca al final de la lista (scroll infinito). */
    fun loadNextPage() {
        val s = _uiState.value
        if (s.isLoading || s.isLoadingMore || s.endReached || s.error != null) return
        when (s.mode) {
            ListMode.ALL -> loadRemotePage()
            ListMode.TYPE -> appendTypePage()
            ListMode.NAME -> Unit
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSearch() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            resetToAll()
            return
        }
        loadJob?.cancel()
        typeResults = emptyList()
        _uiState.update {
            it.copy(
                mode = ListMode.NAME,
                selectedType = null,
                items = emptyList(),
                isLoading = true,
                isLoadingMore = false,
                endReached = true,
                error = null,
                lastSearchedName = query
            )
        }
        loadJob = viewModelScope.launch {
            when (val result = repository.searchByName(query)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(items = result.data, isLoading = false)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun onClearSearch() {
        _uiState.update { it.copy(query = "") }
        if (_uiState.value.mode == ListMode.NAME) resetToAll()
    }

    fun onTypeSelected(type: String?) {
        if (type == null || type == _uiState.value.selectedType) {
            resetToAll()
            return
        }
        loadJob?.cancel()
        typeResults = emptyList()
        _uiState.update {
            it.copy(
                mode = ListMode.TYPE,
                selectedType = type,
                query = "",
                items = emptyList(),
                isLoading = true,
                isLoadingMore = false,
                endReached = false,
                error = null
            )
        }
        loadJob = viewModelScope.launch {
            when (val result = repository.getPokemonByType(type)) {
                is AppResult.Success -> {
                    typeResults = result.data
                    _uiState.update {
                        it.copy(isLoading = false, endReached = result.data.isEmpty())
                    }
                    appendTypePage()
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun retry() {
        val s = _uiState.value
        if (s.types.isEmpty()) loadTypes()
        when (s.mode) {
            ListMode.ALL -> {
                _uiState.update { it.copy(error = null) }
                loadNextPage()
            }
            ListMode.TYPE -> {
                if (typeResults.isEmpty()) {
                    val type = s.selectedType
                    _uiState.update { it.copy(selectedType = null, error = null) }
                    onTypeSelected(type)
                } else {
                    _uiState.update { it.copy(error = null) }
                    loadNextPage()
                }
            }
            ListMode.NAME -> {
                _uiState.update { it.copy(query = s.lastSearchedName) }
                onSearch()
            }
        }
    }

    private fun resetToAll() {
        loadJob?.cancel()
        typeResults = emptyList()
        _uiState.update {
            it.copy(
                mode = ListMode.ALL,
                selectedType = null,
                items = emptyList(),
                isLoading = false,
                isLoadingMore = false,
                endReached = false,
                error = null
            )
        }
        loadNextPage()
    }

    private fun loadRemotePage() {
        val isFirstPage = _uiState.value.items.isEmpty()
        _uiState.update {
            it.copy(isLoading = isFirstPage, isLoadingMore = !isFirstPage, error = null)
        }
        loadJob = viewModelScope.launch {
            val offset = _uiState.value.items.size
            when (val result = repository.getPokemonPage(offset = offset, limit = PAGE_SIZE)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        items = (it.items + result.data.items).distinctBy { p -> p.id },
                        endReached = !result.data.hasMore,
                        isLoading = false,
                        isLoadingMore = false
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isLoadingMore = false, error = result.message)
                }
            }
        }
    }

    private fun appendTypePage() {
        _uiState.update { state ->
            val next = typeResults.drop(state.items.size).take(PAGE_SIZE)
            val newItems = state.items + next
            state.copy(items = newItems, endReached = newItems.size >= typeResults.size)
        }
    }

    private fun loadTypes() {
        viewModelScope.launch {
            val result = repository.getTypes()
            if (result is AppResult.Success) {
                _uiState.update { it.copy(types = result.data) }
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
