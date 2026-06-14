package com.sha5.ticketpigeon.inventory.repository;

import com.sha5.ticketpigeon.inventory.model.BookedSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BookedSeatRepository extends JpaRepository<BookedSeat, UUID> {
    List<BookedSeat> findByShowId(UUID showId);
    boolean existsByShowIdAndSeatIdIn(UUID showId, List<UUID> seatIds);
    void deleteByShowIdAndSeatIdIn(UUID showId, List<UUID> seatIds);
}
