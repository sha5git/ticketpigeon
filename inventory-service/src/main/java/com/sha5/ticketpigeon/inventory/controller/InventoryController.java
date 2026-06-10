package com.sha5.ticketpigeon.inventory.controller;

import com.sha5.ticketpigeon.inventory.dto.SeatAvailabilityResponse;
import com.sha5.ticketpigeon.inventory.dto.SeatLockRequest;
import com.sha5.ticketpigeon.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/lock")
    public ResponseEntity<Boolean> lockSeats(@RequestBody SeatLockRequest request) {
        boolean success = inventoryService.lockSeats(
                request.getShowId(),
                request.getSeatIds(),
                request.getUserId()
        );
        if (success) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmSeats(@RequestBody SeatLockRequest request) {
        inventoryService.confirmSeats(
                request.getShowId(),
                request.getSeatIds(),
                request.getUserId() // using userId field to pass bookingId
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseSeats(@RequestBody SeatLockRequest request) {
        inventoryService.releaseSeats(
                request.getShowId(),
                request.getSeatIds()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<SeatAvailabilityResponse> getSeatAvailability(@PathVariable("showId") UUID showId) {
        SeatAvailabilityResponse response = SeatAvailabilityResponse.builder()
                .showId(showId)
                .lockedSeatIds(inventoryService.getLockedSeats(showId))
                .bookedSeatIds(inventoryService.getBookedSeats(showId))
                .build();
        return ResponseEntity.ok(response);
    }
}
