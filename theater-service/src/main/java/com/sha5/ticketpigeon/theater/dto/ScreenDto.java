package com.sha5.ticketpigeon.theater.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenDto {
    private UUID id;
    private UUID theaterId;
    private String name;
    private Integer totalSeats;
}
