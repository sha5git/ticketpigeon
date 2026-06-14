package com.sha5.ticketpigeon.movie.repository;

import com.sha5.ticketpigeon.movie.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {
}
