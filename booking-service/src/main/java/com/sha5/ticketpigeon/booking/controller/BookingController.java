package com.sha5.ticketpigeon.booking.controller;

import com.sha5.ticketpigeon.booking.dto.BookingRequest;
import com.sha5.ticketpigeon.booking.model.Booking;
import com.sha5.ticketpigeon.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestBody BookingRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new RuntimeException("Access denied: missing authenticated user ID");
        }

        UUID userId = UUID.fromString(userIdHeader);
        Booking booking = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<Booking> confirmBooking(@PathVariable("bookingId") UUID bookingId) {
        Booking booking = bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(booking);
    }
}
