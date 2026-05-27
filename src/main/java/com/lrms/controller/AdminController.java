package com.lrms.controller;

import com.lrms.entity.HousekeepingTask;
import com.lrms.entity.Staff;
import com.lrms.entity.Room;
import com.lrms.entity.Booking;
import com.lrms.entity.Guest;
import com.lrms.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "Staff and Housekeeping management")
public class AdminController {

    private final HousekeepingService housekeepingService;
    private final LodgingService lodgingService;
    private final RestaurantService restaurantService;
    private final ApiUsageService apiUsageService;
    private final BillingService billingService;
    private final GroqService groqService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(HousekeepingService housekeepingService, 
                           LodgingService lodgingService,
                           RestaurantService restaurantService,
                           ApiUsageService apiUsageService,
                           BillingService billingService,
                           GroqService groqService,
                           PasswordEncoder passwordEncoder) {
        this.housekeepingService = housekeepingService;
        this.lodgingService = lodgingService;
        this.restaurantService = restaurantService;
        this.apiUsageService = apiUsageService;
        this.billingService = billingService;
        this.groqService = groqService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("availableRooms", lodgingService.countAvailableRooms());
        stats.put("todayBookings", lodgingService.countTodayBookings());
        stats.put("activeOrders", restaurantService.countActiveOrders());
        stats.put("pendingTasks", housekeepingService.countPendingTasks());
        stats.put("totalApiRequests", apiUsageService.getTotalRequests());
        return stats;
    }

    @GetMapping("/analysis")
    public ResponseEntity<Map<String, Object>> getAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        // 1. Room Status Counts
        List<Room> rooms = lodgingService.getAllRooms();
        Map<String, Long> roomStatus = rooms.stream()
                .collect(Collectors.groupingBy(Room::getStatus, Collectors.counting()));
        for (String status : List.of("AVAILABLE", "OCCUPIED", "CLEANING", "MAINTENANCE")) {
            roomStatus.putIfAbsent(status, 0L);
        }
        analysis.put("roomStatus", roomStatus);

        // 2. Booking Status Counts
        List<Booking> bookings = lodgingService.getAllBookings();
        Map<String, Long> bookingStatus = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));
        for (String status : List.of("PENDING", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED")) {
            bookingStatus.putIfAbsent(status, 0L);
        }
        analysis.put("bookingStatus", bookingStatus);

        // 3. Housekeeping Status Counts
        List<HousekeepingTask> tasks = housekeepingService.getAllTasks();
        Map<String, Long> housekeepingStats = tasks.stream()
                .collect(Collectors.groupingBy(HousekeepingTask::getStatus, Collectors.counting()));
        for (String status : List.of("PENDING", "IN_PROGRESS", "COMPLETED", "APPROVED")) {
            housekeepingStats.putIfAbsent(status, 0L);
        }
        analysis.put("housekeepingStats", housekeepingStats);

        // 4. Revenue Analysis
        analysis.put("revenue", billingService.getRevenueAnalysis());

        // 5. Top Menu Items
        analysis.put("topMenuItems", restaurantService.getTopSellingMenuItems());

        // 6. Guest Stats
        List<Guest> guests = lodgingService.getAllGuests();
        long vipCount = guests.stream().filter(g -> Boolean.TRUE.equals(g.getIsVip())).count();
        long regularCount = guests.size() - vipCount;
        analysis.put("vipCount", vipCount);
        analysis.put("regularCount", regularCount);

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/ai/insights")
    public ResponseEntity<Map<String, String>> getAiInsights() {
        Map<String, String> result = new HashMap<>();
        
        List<Room> rooms = lodgingService.getAllRooms();
        long availableRooms = rooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
        long occupiedRooms = rooms.stream().filter(r -> "OCCUPIED".equals(r.getStatus())).count();
        long cleaningRooms = rooms.stream().filter(r -> "CLEANING".equals(r.getStatus())).count();
        long maintenanceRooms = rooms.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count();

        List<Booking> bookings = lodgingService.getAllBookings();
        long pendingBookings = bookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count();
        long confirmedBookings = bookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus())).count();
        long checkedInBookings = bookings.stream().filter(b -> "CHECKED_IN".equals(b.getStatus())).count();

        List<HousekeepingTask> tasks = housekeepingService.getAllTasks();
        long pendingTasks = tasks.stream().filter(t -> !"COMPLETED".equals(t.getStatus()) && !"APPROVED".equals(t.getStatus())).count();

        Map<String, Object> revenue = billingService.getRevenueAnalysis();
        List<Map<String, Object>> topSelling = restaurantService.getTopSellingMenuItems();

        StringBuilder sellingStr = new StringBuilder();
        for (Map<String, Object> item : topSelling) {
            sellingStr.append("- ").append(item.get("itemName")).append(": ").append(item.get("quantity")).append(" ordered\n");
        }

        String systemPrompt = "You are a professional hotel and restaurant consultant & revenue analyst. " +
                "You analyze hotel metrics (occupancy, housekeeping backlog, food popularity, revenue ratios) " +
                "and generate a short, professional, executive report. Be direct, clear, and action-oriented. " +
                "Do not use markdown headers larger than ###. Format with clean bullet points.";

        String userPrompt = String.format(
                "Analyze the following current hotel and restaurant status and provide operational recommendations:\n\n" +
                "--- Rooms & Occupancy ---\n" +
                "- Total Rooms: %d\n" +
                "- Available Rooms: %d\n" +
                "- Occupied Rooms: %d\n" +
                "- In Cleaning: %d\n" +
                "- In Maintenance: %d\n\n" +
                "--- Bookings Pipeline ---\n" +
                "- Pending: %d\n" +
                "- Confirmed: %d\n" +
                "- Currently Checked-in Guests: %d\n\n" +
                "--- Housekeeping Backlog ---\n" +
                "- Active/Pending Cleaning/Maintenance Tasks: %d\n\n" +
                "--- Financials ---\n" +
                "- Paid Room Revenue: INR %s\n" +
                "- Paid Food & Beverage Revenue: INR %s\n" +
                "- GST Collected: INR %s\n" +
                "- Pending Invoices (Unpaid): INR %s\n\n" +
                "--- Top Restaurant Dishes Ordered ---\n" +
                "%s\n" +
                "Please write a report with:\n" +
                "1. Executive Analytics Summary (key percentages and ratios)\n" +
                "2. Actionable Operational Priorities (cleaning backlog, maintenance room status, bookings confirmation)\n" +
                "3. Revenue & F&B Recommendations (promoting slow menu categories, converting pending bills to paid status)\n",
                rooms.size(), availableRooms, occupiedRooms, cleaningRooms, maintenanceRooms,
                pendingBookings, confirmedBookings, checkedInBookings,
                pendingTasks,
                revenue.get("totalRoomRevenue"), revenue.get("totalFoodRevenue"),
                revenue.get("totalGst"), revenue.get("totalAmountPending"),
                sellingStr.length() > 0 ? sellingStr.toString() : "- No order data yet\n"
        );

        if (!groqService.isKeyConfigured()) {
            String dummyResponse = "### 📋 Executive Analytics Summary (Preview)\n" +
                    "- **Occupancy Rate:** " + (rooms.isEmpty() ? 0 : (occupiedRooms * 100 / rooms.size())) + "% (Occupied: " + occupiedRooms + "/" + rooms.size() + ")\n" +
                    "- **Housekeeping load:** " + pendingTasks + " pending tasks are awaiting staff assignments.\n" +
                    "- **F&B Share of Revenue:** " + (revenue.get("totalFoodRevenue") == null ? "0.00" : revenue.get("totalFoodRevenue")) + " INR vs Room: " + (revenue.get("totalRoomRevenue") == null ? "0.00" : revenue.get("totalRoomRevenue")) + " INR.\n\n" +
                    "### 🧹 Actionable Operational Priorities\n" +
                    (pendingTasks > 2 ? "- **Critical Housekeeping Backlog:** There is a high volume of cleaning tasks. Re-assign staff roles to speed up turnaround.\n" : "- **Housekeeping normal:** Backlog is currently low. Focus on preventive room inspections.\n") +
                    (maintenanceRooms > 0 ? "- **Maintenance alert:** " + maintenanceRooms + " room(s) under maintenance. Coordinate with the technician to restore them to Available status.\n" : "") +
                    (pendingBookings > 0 ? "- **Unconfirmed Bookings:** " + pendingBookings + " booking(s) are still PENDING. Contact guests to confirm check-in dates.\n" : "") +
                    "\n### 💰 Revenue & F&B Recommendations\n" +
                    "- **Food Performance:** Popular items: " + (topSelling.isEmpty() ? "N/A" : topSelling.get(0).get("itemName")) + ". Offer combo meals featuring this item for room service to boost ticket size.\n" +
                    "- **Invoice follow-up:** " + revenue.get("totalAmountPending") + " INR is currently unpaid/pending. Front desk should settle pending check-outs during morning shift.\n\n" +
                    "> ⚠️ *Note: To generate real-time AI analytics customized to your data, configure your GROQ_API_KEY in application.properties.*";
            result.put("analysis", dummyResponse);
            return ResponseEntity.ok(result);
        }

        String aiAnalysis = groqService.callGroq(systemPrompt, userPrompt);
        result.put("analysis", aiAnalysis);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ai/guest-assist/{bookingId}")
    public ResponseEntity<Map<String, String>> getGuestAssist(@PathVariable Long bookingId) {
        Map<String, String> result = new HashMap<>();

        Booking booking = lodgingService.getBookingById(bookingId);
        List<com.lrms.entity.MenuItem> menuItems = restaurantService.getAllMenuItems();

        StringBuilder menuStr = new StringBuilder();
        for (com.lrms.entity.MenuItem item : menuItems) {
            menuStr.append("- ").append(item.getItemName()).append(" (")
                   .append(item.getCategoryName()).append(", Price: ").append(item.getPrice())
                   .append(", ").append(item.getIsVeg() ? "Vegetarian" : "Non-Vegetarian")
                   .append(")\n");
        }

        String systemPrompt = "You are a hospitality concierge specialist and operations coordinator at a premium hotel. " +
                "Given guest booking details, special requests, VIP status, and our hotel's actual menu options, you must output three distinct sections:\n" +
                "1. PERSONALIZED WELCOME CARD: A brief, warm greeting card tailored to the guest. Acknowledge VIP flag or special requests.\n" +
                "2. STAFF ACTION ITEMS: Actionable preparation tasks for Housekeeping or Front Desk.\n" +
                "3. RECOMMENDED DINING OPTIONS: Pick 2-3 specific dishes from our actual restaurant menu that match their preferences or requests. Explain why you picked them.\n" +
                "Do not use markdown headers larger than ###.";

        String userPrompt = String.format(
                "--- Guest Booking Details ---\n" +
                "- Guest Name: %s\n" +
                "- Nationality: %s\n" +
                "- VIP Flag: %s\n" +
                "- Room Number: %s (%s)\n" +
                "- Travel Dates: %s to %s\n" +
                "- Guests: %d Adults, %d Children\n" +
                "- Special Requests: %s\n\n" +
                "--- Hotel Restaurant Menu Items ---\n" +
                "%s\n" +
                "Please output the personalized welcome card, staff action items, and dish recommendations based on the menu list.",
                booking.getGuest().getFullName(),
                booking.getGuest().getNationality() != null ? booking.getGuest().getNationality() : "Not specified",
                booking.getGuest().getIsVip() ? "YES" : "NO",
                booking.getRoom().getRoomNumber(), booking.getRoom().getRoomType().getTypeName(),
                booking.getCheckInDate(), booking.getCheckOutDate(),
                booking.getAdults(), booking.getChildren(),
                booking.getSpecialRequests() != null && !booking.getSpecialRequests().trim().isEmpty() ? booking.getSpecialRequests() : "None",
                menuStr.toString()
        );

        if (!groqService.isKeyConfigured()) {
            String dummyResponse = "### 💌 Personalized Welcome Card (Preview)\n" +
                    "\"Dear " + booking.getGuest().getFullName() + ",\n" +
                    "Welcome to LRMS! We are delighted to host you in Room " + booking.getRoom().getRoomNumber() + " (" + booking.getRoom().getRoomType().getTypeName() + ") from " + booking.getCheckInDate() + " to " + booking.getCheckOutDate() + ". " +
                    (booking.getGuest().getIsVip() ? "As our valued VIP guest, your comfort is our absolute priority. " : "") +
                    (booking.getSpecialRequests() != null && !booking.getSpecialRequests().trim().isEmpty() ? "We have carefully noted your special request: '" + booking.getSpecialRequests() + "' and made necessary preparations." : "If you need anything to make your stay more comfortable, please contact the front desk.") +
                    "\n\nWarm regards,\nLRMS Hotel Team\"\n\n" +
                    "### 🛠️ Staff Action Items\n" +
                    "- **Front Desk:** Prepare express check-in keys for Room " + booking.getRoom().getRoomNumber() + ".\n" +
                    (booking.getGuest().getIsVip() ? "- **Housekeeping:** Double inspect Room " + booking.getRoom().getRoomNumber() + " and place VIP welcome basket.\n" : "") +
                    (booking.getSpecialRequests() != null && !booking.getSpecialRequests().trim().isEmpty() ? "- **Setup Team:** Address special request: \"" + booking.getSpecialRequests() + "\".\n" : "") +
                    "- **Housekeeping:** Ensure room occupancy limit (" + booking.getRoom().getRoomType().getMaxOccupancy() + " guests) setup matches " + (booking.getAdults() + booking.getChildren()) + " guest(s).\n\n" +
                    "### 🍽️ Recommended Dining Options (From Menu)\n" +
                    "- **Dal Makhani:** A premium, rich vegetarian lentil slow-cooked to perfection (Category: Mains). Perfect for a comforting dine-in or room service dinner.\n" +
                    "- **Fresh Lime Soda:** (Category: Beverages). A refreshing welcome drink to recover after a long journey.\n\n" +
                    "> ⚠️ *Note: To generate real-time AI recommendations, configure your GROQ_API_KEY.*";
            result.put("analysis", dummyResponse);
            return ResponseEntity.ok(result);
        }

        String aiAnalysis = groqService.callGroq(systemPrompt, userPrompt);
        result.put("analysis", aiAnalysis);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/external-usage")
    public Map<String, Object> getExternalUsage() {
        Map<String, Object> usage = new HashMap<>();
        usage.put("recent", apiUsageService.getRecentUsage());
        usage.put("stats", apiUsageService.getUsageStats());
        usage.put("total", apiUsageService.getTotalRequests());
        return usage;
    }

    @GetMapping("/housekeeping")
    public List<HousekeepingTask> getAllTasks() { return housekeepingService.getAllTasks(); }

    @PostMapping("/housekeeping")
    public HousekeepingTask createTask(@RequestBody HousekeepingTask task) { return housekeepingService.createTask(task); }

    @PatchMapping("/housekeeping/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        housekeepingService.updateTaskStatus(id, body.get("status"));
        return ResponseEntity.ok().build();
    }

    // ── Staff Management ──

    @GetMapping("/staff")
    public List<Staff> getAllStaff() { return housekeepingService.getAllStaff(); }

    @PostMapping("/staff")
    public Staff createStaff(@RequestBody Map<String, String> body) {
        Staff staff = new Staff();
        staff.setFullName(body.get("fullName"));
        staff.setEmail(body.get("email"));
        staff.setPasswordHash(passwordEncoder.encode(body.get("password")));
        staff.setRole(body.get("role"));
        staff.setIsActive(true);
        return housekeepingService.createStaff(staff);
    }

    @PutMapping("/staff/{id}")
    public Staff updateStaffRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return housekeepingService.updateStaffRole(id, body.get("role"));
    }

    @PatchMapping("/staff/{id}/toggle")
    public Staff toggleStaffActive(@PathVariable Long id) {
        return housekeepingService.toggleStaffActive(id);
    }
}
