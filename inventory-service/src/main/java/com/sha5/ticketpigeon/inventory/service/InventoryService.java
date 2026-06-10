package com.sha5.ticketpigeon.inventory.service;

import com.sha5.ticketpigeon.inventory.model.BookedSeat;
import com.sha5.ticketpigeon.inventory.repository.BookedSeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InventoryService {

    private final StringRedisTemplate redisTemplate;
    private final BookedSeatRepository bookedSeatRepository;
    private final RedisScript<Boolean> lockScript;

    public InventoryService(StringRedisTemplate redisTemplate, BookedSeatRepository bookedSeatRepository) {
        this.redisTemplate = redisTemplate;
        this.bookedSeatRepository = bookedSeatRepository;

        // Lua Script: Checks if any of the keys already exist.
        // If yes, it does nothing and returns false.
        // If none exist, it sets them with the value (userId) and TTL (10 mins) and returns true.
        String script =
                "for i, key in ipairs(KEYS) do " +
                "  if redis.call('EXISTS', key) == 1 then " +
                "    return false " +
                "  end " +
                "end " +
                "for i, key in ipairs(KEYS) do " +
                "  redis.call('SET', key, ARGV[1], 'PX', tonumber(ARGV[2])) " +
                "end " +
                "return true";
        this.lockScript = RedisScript.of(script, Boolean.class);
    }

    /**
     * Attempts to acquire temporary locks in Redis for the specified seats.
     */
    @Transactional
    public boolean lockSeats(UUID showId, List<UUID> seatIds, UUID userId) {
        // 1. Check if any seats are already permanently booked in PostgreSQL database
        boolean alreadyBooked = bookedSeatRepository.existsByShowIdAndSeatIdIn(showId, seatIds);
        if (alreadyBooked) {
            log.warn("Attempt to lock seats failed: some seats are already permanently booked for show: {}", showId);
            return false;
        }

        // 2. Build the list of Redis keys
        List<String> keys = seatIds.stream()
                .map(seatId -> "lock:show:" + showId + ":seat:" + seatId)
                .collect(Collectors.toList());

        // 3. Execute atomic Redis Lua script (TTL: 10 minutes = 600,000 milliseconds)
        Boolean success = redisTemplate.execute(
                lockScript,
                keys,
                userId.toString(),
                "600000"
        );

        boolean locked = success != null && success;
        if (locked) {
            log.info("Successfully locked seats {} for show: {} by user: {}", seatIds, showId, userId);
        } else {
            log.warn("Attempt to lock seats {} failed: some seats are temporarily locked for show: {}", seatIds, showId);
        }
        return locked;
    }

    /**
     * Commits the seat locks as permanent bookings.
     */
    @Transactional
    public void confirmSeats(UUID showId, List<UUID> seatIds, UUID bookingId) {
        // 1. Save to the database
        List<BookedSeat> bookedSeats = seatIds.stream()
                .map(seatId -> BookedSeat.builder()
                        .showId(showId)
                        .seatId(seatId)
                        .bookingId(bookingId)
                        .build())
                .collect(Collectors.toList());

        bookedSeatRepository.saveAll(bookedSeats);
        log.info("Saved permanently booked seats {} for show: {} under booking: {}", seatIds, showId, bookingId);

        // 2. Delete the Redis temporary locks
        List<String> keys = seatIds.stream()
                .map(seatId -> "lock:show:" + showId + ":seat:" + seatId)
                .collect(Collectors.toList());
        redisTemplate.delete(keys);
    }

    /**
     * Releases the temporary seat locks in Redis.
     */
    public void releaseSeats(UUID showId, List<UUID> seatIds) {
        List<String> keys = seatIds.stream()
                .map(seatId -> "lock:show:" + showId + ":seat:" + seatId)
                .collect(Collectors.toList());
        redisTemplate.delete(keys);
        log.info("Released temporary seat locks {} for show: {}", seatIds, showId);
    }

    /**
     * Fetches all locked and booked seat IDs for a given show.
     */
    public List<UUID> getBookedSeats(UUID showId) {
        return bookedSeatRepository.findByShowId(showId).stream()
                .map(BookedSeat::getSeatId)
                .collect(Collectors.toList());
    }

    public List<UUID> getLockedSeats(UUID showId) {
        Set<String> keys = redisTemplate.keys("lock:show:" + showId + ":seat:*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return keys.stream()
                .map(key -> {
                    String[] parts = key.split(":");
                    return UUID.fromString(parts[parts.length - 1]);
                })
                .collect(Collectors.toList());
    }
}
