package com.sha5.ticketpigeon.booking.service;

import com.sha5.ticketpigeon.booking.dto.BookingRequest;
import com.sha5.ticketpigeon.booking.dto.SeatLockRequest;
import com.sha5.ticketpigeon.booking.model.Booking;
import com.sha5.ticketpigeon.booking.model.BookingSeat;
import com.sha5.ticketpigeon.booking.model.BookingStatus;
import com.sha5.ticketpigeon.booking.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestClient restClient;

    public BookingService(BookingRepository bookingRepository,
                          RestClient.Builder restClientBuilder,
                          @Value("${gateway.secret:ELRpz7MMNGUwuQ4jXdEX4JojHTeQKGRICEceYEpupUT}") String gatewaySecret) {
        this.bookingRepository = bookingRepository;
        this.restClient = restClientBuilder
                .baseUrl("http://inventory-service")
                .defaultHeader("X-Gateway-Secret", gatewaySecret)
                .build();
    }

    /**
     * Creates a new booking in PENDING_PAYMENT status and requests seat locking from inventory-service.
     */
    @Transactional
    public Booking createBooking(BookingRequest request, UUID userId) {
        log.info("Creating booking for user: {}, show: {}, seats: {}", userId, request.getShowId(), request.getSeatIds());

        // 1. Attempt to lock seats downstream in inventory-service
        SeatLockRequest lockRequest = SeatLockRequest.builder()
                .showId(request.getShowId())
                .seatIds(request.getSeatIds())
                .userId(userId)
                .build();

        try {
            ResponseEntity<Boolean> response = restClient.post()
                    .uri("/api/v1/inventory/lock")
                    .body(lockRequest)
                    .retrieve()
                    .toEntity(Boolean.class);

            if (!response.getStatusCode().is2xxSuccessful() || !Boolean.TRUE.equals(response.getBody())) {
                throw new RuntimeException("Selected seats are temporarily locked or permanently booked. Please choose other seats.");
            }
        } catch (Exception e) {
            log.error("Failed to acquire seat locks from inventory-service", e);
            throw new RuntimeException("Selected seats are temporarily locked or permanently booked. Please choose other seats.");
        }

        // 2. Calculate prices (mocking 150.00 flat price per seat in Phase 2)
        BigDecimal seatPrice = new BigDecimal("150.00");
        BigDecimal totalAmount = seatPrice.multiply(new BigDecimal(request.getSeatIds().size()));

        // 3. Build Booking structure
        Booking booking = Booking.builder()
                .userId(userId)
                .showId(request.getShowId())
                .status(BookingStatus.PENDING_PAYMENT)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        List<BookingSeat> seats = request.getSeatIds().stream()
                .map(seatId -> BookingSeat.builder()
                        .booking(booking)
                        .seatId(seatId)
                        .price(seatPrice)
                        .build())
                .collect(Collectors.toList());

        booking.setSeats(seats);

        // 4. Save to DB
        return bookingRepository.save(booking);
    }

    /**
     * Confirms the booking (called after simulated payment validation).
     */
    @Transactional
    public Booking confirmBooking(UUID bookingId) {
        log.info("Confirming booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Booking is not in PENDING_PAYMENT status. Current status: " + booking.getStatus());
        }

        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Booking has expired and cannot be confirmed.");
        }

        // 1. Confirm seats downstream in inventory-service (passing bookingId as the userId to associate booking)
        List<UUID> seatIds = booking.getSeats().stream()
                .map(BookingSeat::getSeatId)
                .collect(Collectors.toList());

        SeatLockRequest confirmRequest = SeatLockRequest.builder()
                .showId(booking.getShowId())
                .seatIds(seatIds)
                .userId(bookingId)
                .build();

        try {
            restClient.post()
                    .uri("/api/v1/inventory/confirm")
                    .body(confirmRequest)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to confirm seat bookings in inventory-service for booking: {}", bookingId, e);
            throw new RuntimeException("Failed to complete booking confirmation downstream: " + e.getMessage());
        }

        // 2. Set status to CONFIRMED
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    /**
     * Background job running every minute to cancel expired bookings and release locks.
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(
                BookingStatus.PENDING_PAYMENT,
                now
        );

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("Found {} expired pending bookings to cancel at {}", expiredBookings.size(), now);

        for (Booking booking : expiredBookings) {
            log.info("Cancelling expired booking: {}", booking.getId());
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            // Release locks downstream in inventory-service
            List<UUID> seatIds = booking.getSeats().stream()
                    .map(BookingSeat::getSeatId)
                    .collect(Collectors.toList());

            SeatLockRequest releaseRequest = SeatLockRequest.builder()
                    .showId(booking.getShowId())
                    .seatIds(seatIds)
                    .userId(booking.getUserId())
                    .build();

            try {
                restClient.post()
                        .uri("/api/v1/inventory/release")
                        .body(releaseRequest)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Successfully released seats downstream for expired booking: {}", booking.getId());
            } catch (Exception e) {
                log.error("Failed to release seat locks downstream for expired booking: {}", booking.getId(), e);
            }
        }
    }
}
