package com.sha5.ticketpigeon.theater.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Defines a single row in a bulk seat creation request.
 * Example: { "row": "A", "seatCount": 12, "seatType": "RECLINER", "price": 500 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatRowLayout {
    private String row;
    private Integer seatCount;
    private String seatType;
    private BigDecimal price;
}
