package com.sha5.ticketpigeon.theater.repository;

import com.sha5.ticketpigeon.theater.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByScreenId(UUID screenId);
    List<Seat> findAllByIdIn(List<UUID> ids);
}
