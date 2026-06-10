package com.sha5.ticketpigeon.theater.repository;

import com.sha5.ticketpigeon.theater.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShowRepository extends JpaRepository<Show, UUID> {
    List<Show> findByScreenId(UUID screenId);
    List<Show> findByMovieId(UUID movieId);
}
