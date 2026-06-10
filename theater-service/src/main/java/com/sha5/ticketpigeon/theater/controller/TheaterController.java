package com.sha5.ticketpigeon.theater.controller;

import com.sha5.ticketpigeon.theater.dto.*;
import com.sha5.ticketpigeon.theater.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    // ── Theater ──────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<TheaterDto> addTheater(@RequestBody TheaterDto theaterDto) {
        return new ResponseEntity<>(theaterService.addTheater(theaterDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheaterDto> getTheater(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @GetMapping
    public ResponseEntity<List<TheaterDto>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    // ── Screen ───────────────────────────────────────────────────

    @PostMapping("/{theaterId}/screens")
    public ResponseEntity<ScreenDto> addScreen(
            @PathVariable("theaterId") UUID theaterId,
            @RequestBody ScreenDto screenDto) {
        return new ResponseEntity<>(theaterService.addScreen(theaterId, screenDto), HttpStatus.CREATED);
    }

    @GetMapping("/{theaterId}/screens")
    public ResponseEntity<List<ScreenDto>> getScreens(@PathVariable("theaterId") UUID theaterId) {
        return ResponseEntity.ok(theaterService.getScreensByTheater(theaterId));
    }

    // ── Seat (bulk) ───────────────────────────────────────────────

    @PostMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<SeatDto>> addSeats(
            @PathVariable("screenId") UUID screenId,
            @RequestBody BulkSeatRequest request) {
        return new ResponseEntity<>(theaterService.addSeatsInBulk(screenId, request), HttpStatus.CREATED);
    }

    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<SeatDto>> getSeats(@PathVariable("screenId") UUID screenId) {
        return ResponseEntity.ok(theaterService.getSeatsByScreen(screenId));
    }
}
