package com.lrms.repository;

import com.lrms.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(String status);
    
    List<Booking> findByGuestGuestId(Long guestId);
    
    @Query("SELECT b FROM Booking b WHERE b.room.roomId = :roomId AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND (b.checkInDate < :checkOut AND b.checkOutDate > :checkIn)")
    List<Booking> findOverlappingBookings(Long roomId, LocalDate checkIn, LocalDate checkOut);
}
