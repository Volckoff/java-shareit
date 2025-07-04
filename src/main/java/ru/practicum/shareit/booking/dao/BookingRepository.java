package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {


    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Status status);
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime start);
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime end);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long userId, Status status);
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime start);
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime end);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.booker.id = :userId
            AND b.start <= CURRENT_TIMESTAMP
            AND b.end >= CURRENT_TIMESTAMP
            AND b.status = APPROVED
            ORDER BY b.start DESC
            """)
    List<Booking> findCurrentBookingsByUser(@Param("userId") Long userId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.owner.id = :ownerId
            AND b.start <= CURRENT_TIMESTAMP
            AND b.end >= CURRENT_TIMESTAMP
            AND b.status = APPROVED
            ORDER BY b.start DESC
            """)
    List<Booking> findCurrentBookingsByOwner(@Param("ownerId") Long ownerId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id IN :itemIds
            AND b.status = APPROVED
            AND b.end <= CURRENT_TIMESTAMP
            AND b.id IN (
                SELECT MAX(b2.id) FROM Booking b2
                WHERE b2.item.id = b.item.id
                AND b2.status = APPROVED
                AND b2.end <= CURRENT_TIMESTAMP
                GROUP BY b2.item.id
            )
            """)
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id IN :itemIds
            AND b.status = APPROVED
            AND b.start > CURRENT_TIMESTAMP
            AND b.id IN (
                SELECT MIN(b2.id) FROM Booking b2
                WHERE b2.item.id = b.item.id
                AND b2.status = APPROVED
                AND b2.start > CURRENT_TIMESTAMP
                GROUP BY b2.item.id
            )
            """)
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds);
}
