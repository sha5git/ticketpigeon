package com.sha5.ticketpigeon.theater.repository;

import com.sha5.ticketpigeon.theater.model.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
public interface ScreenRepository extends JpaRepository<Screen, UUID> {
    List<Screen> findByTheaterId(UUID theaterId);
}
