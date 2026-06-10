package com.sha5.ticketpigeon.theater.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Rich response DTO returned by GET /api/v1/theaters/shows/{showId}.
 *
 * This is the primary cross-service contract: booking-service calls this endpoint
 * to validate that a showId exists and that all requested seatIds belong to the
 * show's screen. Each SeatDto also carries the price, which booking-service uses
 * to calculate the total booking amount (replacing the hardcoded 150.00).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowDetailDto {
    private UUID id;
    private UUID movieId;
    private UUID screenId;
    private UUID theaterId;
    private LocalDateTime startTime;

    /** All physical seats available on this show's screen, each with their price. */
    private List<SeatDto> seats;
}
