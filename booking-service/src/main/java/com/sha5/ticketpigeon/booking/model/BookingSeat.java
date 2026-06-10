package com.sha5.ticketpigeon.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "booking_seats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Booking booking;

    @Column(nullable = false)
    private UUID seatId;

    @Column(nullable = false)
    private BigDecimal price;
}
