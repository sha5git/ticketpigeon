package com.sha5.ticketpigeon.theater.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "theaters",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_theater_name_city_address",
            columnNames = {"name", "city", "address"}
        )
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String name;
    private String city;
    private String address;
    
    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL)
    private List<Screen> screens;
}
