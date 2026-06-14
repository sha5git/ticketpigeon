package com.sha5.ticketpigeon.theater.repository;

import com.sha5.ticketpigeon.theater.model.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TheaterRepository extends JpaRepository<Theater, UUID> {
}
