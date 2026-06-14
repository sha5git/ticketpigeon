package com.sha5.ticketpigeon.theater.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
    name = "seats",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_seat_screen_row_number",
            columnNames = {"screen_id", "row_id", "seat_number"}
        )
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    @JsonIgnore
    private Screen screen;

    @Column(name = "row_id")
    private String rowId;
    private Integer seatNumber;
    private String type;

    /** Price for this specific seat, set at bulk creation time based on row layout. */
    @Column(nullable = false)
    private BigDecimal price;
}
