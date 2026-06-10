package com.sha5.ticketpigeon.inventory.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "booked_seats",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"showId", "seatId"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID showId;

    @Column(nullable = false)
    private UUID seatId;

    @Column(nullable = false)
    private UUID bookingId;
}
