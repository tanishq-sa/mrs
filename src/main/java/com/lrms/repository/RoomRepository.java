package com.lrms.repository;

import com.lrms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByStatus(String status);

    @Query("SELECT r FROM Room r WHERE r.isActive = true AND (:typeId IS NULL OR r.roomType.roomTypeId = :typeId) " +
           "AND r.roomId NOT IN (SELECT b.room.roomId FROM Booking b WHERE b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND (b.checkInDate < :checkOut AND b.checkOutDate > :checkIn))")
    List<Room> findAvailableRooms(@Param("typeId") Long typeId, @Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);
}
