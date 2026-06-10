package com.sha5.ticketpigeon.theater.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {
    private UUID id;
    private UUID screenId;
    private String rowId;
    private Integer seatNumber;
    private String type;
    private BigDecimal price;
}
