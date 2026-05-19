package com.lrms.service;

import com.lrms.GlobalExceptionHandler;
import com.lrms.entity.Booking;
import com.lrms.entity.Guest;
import com.lrms.entity.Room;
import com.lrms.repository.BookingRepository;
import com.lrms.repository.GuestRepository;
import com.lrms.repository.RoomRepository;
import com.lrms.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LodgingService {

    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;
    private final BillingService billingService;
    private final HousekeepingService housekeepingService;

    public LodgingService(GuestRepository guestRepository, RoomRepository roomRepository,
                          RoomTypeRepository roomTypeRepository, BookingRepository bookingRepository,
                          BillingService billingService, HousekeepingService housekeepingService) {
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.bookingRepository = bookingRepository;
        this.billingService = billingService;
        this.housekeepingService = housekeepingService;
    }

    // --- Guest Operations ---
    public List<Guest> getAllGuests() { return guestRepository.findAll(); }
    public Guest saveGuest(Guest guest) { return guestRepository.save(guest); }
    public Guest getGuestById(Long id) {
        return guestRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Guest not found"));
    }
    public Guest updateGuest(Long id, Guest updatedGuest) {
        Guest existing = getGuestById(id);
        existing.setFullName(updatedGuest.getFullName());
        existing.setEmail(updatedGuest.getEmail());
        existing.setPhone(updatedGuest.getPhone());
        existing.setIdProofType(updatedGuest.getIdProofType());
        existing.setIdProofNumber(updatedGuest.getIdProofNumber());
        existing.setNationality(updatedGuest.getNationality());
        existing.setIsVip(updatedGuest.getIsVip());
        return guestRepository.save(existing);
    }
    public List<Booking> getBookingHistory(Long guestId) {
        return bookingRepository.findByGuestGuestId(guestId);
    }

    // --- Room Operations ---
    public List<Room> getAllRooms() { return roomRepository.findAll(); }
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, Long typeId) {
        return roomRepository.findAvailableRooms(typeId, checkIn, checkOut);
    }
    public Room saveRoom(Room room) { return roomRepository.save(room); }
    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Room not found"));
    }
    public void updateRoomStatus(Long id, String status) {
        Room room = getRoomById(id);
        room.setStatus(status);
        roomRepository.save(room);
    }

    // --- Booking Operations ---
    public List<Booking> getAllBookings() { return bookingRepository.findAll(); }
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Booking not found"));
    }

    public Booking createBooking(Booking booking) {
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                booking.getRoom().getRoomId(), booking.getCheckInDate(), booking.getCheckOutDate());
        if (!overlaps.isEmpty()) {
            throw new GlobalExceptionHandler.BookingConflictException("Room is already booked for these dates");
        }
        return bookingRepository.save(booking);
    }

    public Booking updateBooking(Long id, Booking updatedBooking) {
        Booking existing = getBookingById(id);
        
        boolean roomChanged = !existing.getRoom().getRoomId().equals(updatedBooking.getRoom().getRoomId());
        boolean datesChanged = !existing.getCheckInDate().equals(updatedBooking.getCheckInDate()) || 
                               !existing.getCheckOutDate().equals(updatedBooking.getCheckOutDate());
        
        if (roomChanged || datesChanged) {
            List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                    updatedBooking.getRoom().getRoomId(), updatedBooking.getCheckInDate(), updatedBooking.getCheckOutDate());
            overlaps.removeIf(b -> b.getBookingId().equals(id));
            if (!overlaps.isEmpty()) {
                throw new GlobalExceptionHandler.BookingConflictException("Room is already booked for these dates");
            }
        }
        
        existing.setRoom(updatedBooking.getRoom());
        existing.setCheckInDate(updatedBooking.getCheckInDate());
        existing.setCheckOutDate(updatedBooking.getCheckOutDate());
        existing.setAdults(updatedBooking.getAdults());
        existing.setChildren(updatedBooking.getChildren());
        existing.setSpecialRequests(updatedBooking.getSpecialRequests());
        existing.setTotalAmount(updatedBooking.getTotalAmount());
        
        return bookingRepository.save(existing);
    }

    public void checkIn(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!"CONFIRMED".equals(booking.getStatus()) && !"PENDING".equals(booking.getStatus())) {
            throw new IllegalStateException("Cannot check in from status: " + booking.getStatus());
        }
        booking.setStatus("CHECKED_IN");
        booking.setActualCheckIn(LocalDateTime.now());
        booking.getRoom().setStatus("OCCUPIED");
        bookingRepository.save(booking);
    }

    public void checkOut(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!"CHECKED_IN".equals(booking.getStatus())) {
            throw new IllegalStateException("Cannot check out from status: " + booking.getStatus());
        }
        booking.setStatus("CHECKED_OUT");
        booking.setActualCheckOut(LocalDateTime.now());
        booking.getRoom().setStatus("CLEANING");
        bookingRepository.save(booking);
        
        billingService.generateBillForBooking(bookingId);
        housekeepingService.createCleaningTask(booking.getRoom());
    }
    
    public void cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    public long countAvailableRooms() {
        return roomRepository.findAll().stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
    }

    public long countTodayBookings() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findAll().stream()
                .filter(b -> b.getCheckInDate().equals(today) && !"CANCELLED".equals(b.getStatus()))
                .count();
    }
}
