package com.sha5.ticketpigeon.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private UUID id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String language;
    private String genre;
    private LocalDate releaseDate;
}
