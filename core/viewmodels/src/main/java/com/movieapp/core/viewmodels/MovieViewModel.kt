package com.movieapp.core.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.core.usecases.GetMostPopularMoviesUseCase
import com.movieapp.core.usecases.GetTopRatedMoviesUseCase
import com.movieapp.core.viewmodels.enums.SortType
import com.movieapp.core.viewmodels.mapper.toMovieUiStateList
import com.movieapp.core.viewmodels.uistate.MovieMainUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val getMostPopularMoviesUseCase: GetMostPopularMoviesUseCase,
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase,
) : ViewModel() {

    private val _movieUiState = MutableStateFlow(MovieMainUIState())
    val movieUiState get() = _movieUiState.asStateFlow()

    private val _selectedSortType = MutableStateFlow(SortType.MOST_POPULAR)
    val selectedSortType get() = _selectedSortType.asStateFlow()

    private val _showDropdown = Channel<Boolean>()
    val showDropdown get() = _showDropdown.receiveAsFlow()

    init {
        fetchMovies()
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            try {
                val movies = when (_selectedSortType.value) {
                    SortType.MOST_POPULAR -> getMostPopularMoviesUseCase()
                    SortType.TOP_RATED -> getTopRatedMoviesUseCase()
                }
                _movieUiState.update {
                    it.copy(
                        movies = movies.toMovieUiStateList(),
                        isLoading = false,
                        isError = false,
                        isSuccess = true,
                    )
                }
            } catch (e: Exception) {
                _movieUiState.value = MovieMainUIState(
                    isError = true,
                    isLoading = false,
                    isSuccess = false,
                    movies = emptyList(),
                )
            }
        }
    }

    fun retryLoadMovies() {
        fetchMovies()
    }

    fun setSelectedSortType(sortType: SortType) {
        if (_selectedSortType.value != sortType) {
            _selectedSortType.value = sortType
            fetchMovies()
        }
    }

    fun setShowDropdown(show: Boolean) {
        viewModelScope.launch {
            _showDropdown.send(show)
        }
    }
}
