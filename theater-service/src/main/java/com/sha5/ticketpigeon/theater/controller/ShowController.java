package com.sha5.ticketpigeon.theater.controller;

import com.sha5.ticketpigeon.theater.dto.ShowDetailDto;
import com.sha5.ticketpigeon.theater.dto.ShowDto;
import com.sha5.ticketpigeon.theater.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/theaters/shows")
@RequiredArgsConstructor
public class ShowController {

    private final TheaterService theaterService;

    /** Create a new show on a screen. Requires ADMIN or THEATER_ADMIN role (enforced by gateway). */
    @PostMapping
    public ResponseEntity<ShowDto> addShow(@RequestBody ShowDto showDto) {
        return new ResponseEntity<>(theaterService.addShow(showDto), HttpStatus.CREATED);
    }

    /**
     * List all shows. Optionally filter by movieId.
     * Public endpoint — no auth required (GET on /api/v1/theaters/** is public).
     */
    @GetMapping
    public ResponseEntity<List<ShowDto>> getAllShows(
            @RequestParam(value = "movieId", required = false) UUID movieId) {
        return ResponseEntity.ok(theaterService.getAllShows(Optional.ofNullable(movieId)));
    }

    /**
     * Get full show details including all seats on its screen with their prices.
     * This is the primary cross-service API consumed by booking-service (Phase 2.5)
     * to validate showId + seatIds and calculate booking total amount.
     */
    @GetMapping("/{showId}")
    public ResponseEntity<ShowDetailDto> getShow(@PathVariable("showId") UUID showId) {
        return ResponseEntity.ok(theaterService.getShowById(showId));
    }
}
