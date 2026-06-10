package com.sha5.ticketpigeon.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityResponse {
    private UUID showId;
    private List<UUID> lockedSeatIds;
    private List<UUID> bookedSeatIds;
}
