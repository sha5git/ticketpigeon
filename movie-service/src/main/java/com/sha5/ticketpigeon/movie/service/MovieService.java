package com.sha5.ticketpigeon.movie.service;

import com.sha5.ticketpigeon.common.exception.ResourceNotFoundException;
import com.sha5.ticketpigeon.movie.dto.MovieDto;
import com.sha5.ticketpigeon.movie.model.Movie;
import com.sha5.ticketpigeon.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    
    private final MovieRepository movieRepository;

    public MovieDto addMovie(MovieDto movieDto) {
        Movie movie = Movie.builder()
                .title(movieDto.getTitle())
                .description(movieDto.getDescription())
                .durationMinutes(movieDto.getDurationMinutes())
                .language(movieDto.getLanguage())
                .genre(movieDto.getGenre())
                .releaseDate(movieDto.getReleaseDate())
                .build();
        
        movie = movieRepository.save(movie);
        return mapToDto(movie);
    }

    public MovieDto getMovieById(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return mapToDto(movie);
    }

    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private MovieDto mapToDto(Movie movie) {
        return MovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .language(movie.getLanguage())
                .genre(movie.getGenre())
                .releaseDate(movie.getReleaseDate())
                .build();
    }
}
