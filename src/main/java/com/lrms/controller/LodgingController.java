package com.lrms.controller;

import com.lrms.entity.Booking;
import com.lrms.entity.Guest;
import com.lrms.entity.Room;
import com.lrms.service.LodgingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Lodging", description = "Management of Rooms, Bookings, and Guests")
public class LodgingController {

    private final LodgingService lodgingService;

    public LodgingController(LodgingService lodgingService) {
        this.lodgingService = lodgingService;
    }

    // --- Rooms ---
    @GetMapping("/rooms")
    public List<Room> getAllRooms() { return lodgingService.getAllRooms(); }

    @GetMapping("/rooms/available")
    public List<Room> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Long typeId) {
        return lodgingService.getAvailableRooms(checkIn, checkOut, typeId);
    }

    @PostMapping("/rooms")
    public Room createRoom(@RequestBody Room room) { return lodgingService.saveRoom(room); }

    @PatchMapping("/rooms/{id}/status")
    public ResponseEntity<?> updateRoomStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        lodgingService.updateRoomStatus(id, statusMap.get("status"));
        return ResponseEntity.ok().build();
    }

    // --- Guests ---
    @GetMapping("/guests")
    public List<Guest> getAllGuests() { return lodgingService.getAllGuests(); }

    @PostMapping("/guests")
    public Guest createGuest(@RequestBody Guest guest) { return lodgingService.saveGuest(guest); }

    @PutMapping("/guests/{id}")
    public Guest updateGuest(@PathVariable Long id, @RequestBody Guest guest) {
        return lodgingService.updateGuest(id, guest);
    }

    @GetMapping("/guests/{id}/bookings")
    public List<Booking> getGuestBookings(@PathVariable Long id) {
        return lodgingService.getBookingHistory(id);
    }

    // --- Bookings ---
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() { return lodgingService.getAllBookings(); }

    @PostMapping("/bookings")
    public Booking createBooking(@RequestBody Booking booking) { return lodgingService.createBooking(booking); }

    @PutMapping("/bookings/{id}")
    public Booking updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        return lodgingService.updateBooking(id, booking);
    }

    @PostMapping("/bookings/{id}/checkin")
    public ResponseEntity<?> checkIn(@PathVariable Long id) {
        lodgingService.checkIn(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bookings/{id}/checkout")
    public ResponseEntity<?> checkOut(@PathVariable Long id) {
        lodgingService.checkOut(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        lodgingService.cancelBooking(id);
        return ResponseEntity.ok().build();
    }
}
