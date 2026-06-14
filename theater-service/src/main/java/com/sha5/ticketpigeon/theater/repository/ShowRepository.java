package com.sha5.ticketpigeon.theater.repository;

import com.sha5.ticketpigeon.theater.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShowRepository extends JpaRepository<Show, UUID> {
    List<Show> findByScreenId(UUID screenId);
    List<Show> findByMovieId(UUID movieId);
}
