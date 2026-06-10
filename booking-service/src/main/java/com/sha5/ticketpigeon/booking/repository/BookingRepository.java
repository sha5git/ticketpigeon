package com.sha5.ticketpigeon.booking.repository;

import com.sha5.ticketpigeon.booking.model.Booking;
import com.sha5.ticketpigeon.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime time);
}
