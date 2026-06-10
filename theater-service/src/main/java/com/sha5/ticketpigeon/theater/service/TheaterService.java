package com.sha5.ticketpigeon.theater.service;

import com.sha5.ticketpigeon.common.exception.ResourceNotFoundException;
import com.sha5.ticketpigeon.theater.dto.*;
import com.sha5.ticketpigeon.theater.model.Screen;
import com.sha5.ticketpigeon.theater.model.Seat;
import com.sha5.ticketpigeon.theater.model.Show;
import com.sha5.ticketpigeon.theater.model.Theater;
import com.sha5.ticketpigeon.theater.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;

    // ─────────────────────────────────────────────────────────────
    // Theater
    // ─────────────────────────────────────────────────────────────

    public TheaterDto addTheater(TheaterDto dto) {
        Theater theater = Theater.builder()
                .name(dto.getName())
                .city(dto.getCity())
                .address(dto.getAddress())
                .build();
        theater = theaterRepository.save(theater);
        return mapTheaterToDto(theater);
    }

    public TheaterDto getTheaterById(UUID id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        return mapTheaterToDto(theater);
    }

    public List<TheaterDto> getAllTheaters() {
        return theaterRepository.findAll().stream()
                .map(this::mapTheaterToDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // Screen
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public ScreenDto addScreen(UUID theaterId, ScreenDto dto) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + theaterId));

        Screen screen = Screen.builder()
                .theater(theater)
                .name(dto.getName())
                .totalSeats(dto.getTotalSeats())
                .build();

        screen = screenRepository.save(screen);
        return mapScreenToDto(screen);
    }

    public List<ScreenDto> getScreensByTheater(UUID theaterId) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater not found with id: " + theaterId);
        }
        return screenRepository.findByTheaterId(theaterId).stream()
                .map(this::mapScreenToDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // Seat (bulk creation)
    // ─────────────────────────────────────────────────────────────

    /**
     * Generates seats for a screen from a row-based layout.
     * For each row, seats are numbered 1..seatCount.
     * Example: row "A" with seatCount=12 → A1, A2, ... A12.
     */
    @Transactional
    public List<SeatDto> addSeatsInBulk(UUID screenId, BulkSeatRequest request) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + screenId));

        List<Seat> seatsToSave = new ArrayList<>();
        for (SeatRowLayout row : request.getLayout()) {
            for (int seatNum = 1; seatNum <= row.getSeatCount(); seatNum++) {
                seatsToSave.add(Seat.builder()
                        .screen(screen)
                        .rowId(row.getRow())
                        .seatNumber(seatNum)
                        .type(row.getSeatType())
                        .price(row.getPrice())
                        .build());
            }
        }

        List<Seat> saved = seatRepository.saveAll(seatsToSave);
        return saved.stream().map(this::mapSeatToDto).collect(Collectors.toList());
    }

    public List<SeatDto> getSeatsByScreen(UUID screenId) {
        if (!screenRepository.existsById(screenId)) {
            throw new ResourceNotFoundException("Screen not found with id: " + screenId);
        }
        return seatRepository.findByScreenId(screenId).stream()
                .map(this::mapSeatToDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // Show
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public ShowDto addShow(ShowDto dto) {
        Screen screen = screenRepository.findById(dto.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + dto.getScreenId()));

        Show show = Show.builder()
                .movieId(dto.getMovieId())
                .screen(screen)
                .startTime(dto.getStartTime())
                .build();

        show = showRepository.save(show);
        return mapShowToDto(show);
    }

    public List<ShowDto> getAllShows(Optional<UUID> movieId) {
        List<Show> shows = movieId
                .map(showRepository::findByMovieId)
                .orElseGet(showRepository::findAll);
        return shows.stream().map(this::mapShowToDto).collect(Collectors.toList());
    }

    /**
     * Returns full show details including all seats on its screen.
     * This is the primary API consumed by booking-service to validate
     * showId existence and verify that requested seatIds belong to the show.
     */
    @Transactional(readOnly = true)
    public ShowDetailDto getShowById(UUID showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));

        List<SeatDto> seats = seatRepository.findByScreenId(show.getScreen().getId()).stream()
                .map(this::mapSeatToDto)
                .collect(Collectors.toList());

        return ShowDetailDto.builder()
                .id(show.getId())
                .movieId(show.getMovieId())
                .screenId(show.getScreen().getId())
                .theaterId(show.getScreen().getTheater().getId())
                .startTime(show.getStartTime())
                .seats(seats)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // Mapping helpers
    // ─────────────────────────────────────────────────────────────

    private TheaterDto mapTheaterToDto(Theater theater) {
        return TheaterDto.builder()
                .id(theater.getId())
                .name(theater.getName())
                .city(theater.getCity())
                .address(theater.getAddress())
                .build();
    }

    private ScreenDto mapScreenToDto(Screen screen) {
        return ScreenDto.builder()
                .id(screen.getId())
                .theaterId(screen.getTheater().getId())
                .name(screen.getName())
                .totalSeats(screen.getTotalSeats())
                .build();
    }

    private SeatDto mapSeatToDto(Seat seat) {
        return SeatDto.builder()
                .id(seat.getId())
                .screenId(seat.getScreen().getId())
                .rowId(seat.getRowId())
                .seatNumber(seat.getSeatNumber())
                .type(seat.getType())
                .price(seat.getPrice())
                .build();
    }

    private ShowDto mapShowToDto(Show show) {
        return ShowDto.builder()
                .id(show.getId())
                .movieId(show.getMovieId())
                .screenId(show.getScreen().getId())
                .startTime(show.getStartTime())
                .build();
    }
}
