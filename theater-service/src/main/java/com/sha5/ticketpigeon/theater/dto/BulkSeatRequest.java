package com.sha5.ticketpigeon.theater.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body for bulk seat creation on a screen.
 * The screenId comes from the path variable, not this body.
 *
 * Example:
 * {
 *   "layout": [
 *     { "row": "A", "seatCount": 12, "seatType": "RECLINER", "price": 500 },
 *     { "row": "B", "seatCount": 12, "seatType": "PREMIUM",  "price": 350 },
 *     { "row": "C", "seatCount": 14, "seatType": "REGULAR",  "price": 250 }
 *   ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkSeatRequest {
    private List<SeatRowLayout> layout;
}
