package com.moataz.domain.usecase

import com.moataz.domain.entity.MovieEntity
import com.moataz.domain.repository.MovieRepository
import javax.inject.Inject

class GetTopRatedMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
) {
    suspend operator fun invoke(): List<MovieEntity> {
        return movieRepository.getTopRatedMovies()
    }
}