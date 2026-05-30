package com.sha5.ticketpigeon.theater.service;

import com.sha5.ticketpigeon.common.exception.ResourceNotFoundException;
import com.sha5.ticketpigeon.theater.dto.TheaterDto;
import com.sha5.ticketpigeon.theater.model.Theater;
import com.sha5.ticketpigeon.theater.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;

    public TheaterDto addTheater(TheaterDto dto) {
        Theater theater = Theater.builder()
                .name(dto.getName())
                .city(dto.getCity())
                .address(dto.getAddress())
                .build();
        
        theater = theaterRepository.save(theater);
        return mapToDto(theater);
    }

    public TheaterDto getTheaterById(UUID id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        return mapToDto(theater);
    }

    public List<TheaterDto> getAllTheaters() {
        return theaterRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private TheaterDto mapToDto(Theater theater) {
        return TheaterDto.builder()
                .id(theater.getId())
                .name(theater.getName())
                .city(theater.getCity())
                .address(theater.getAddress())
                .build();
    }
}
