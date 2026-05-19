package com.lrms.service;

import com.lrms.GlobalExceptionHandler;
import com.lrms.entity.*;
import com.lrms.repository.BillRepository;
import com.lrms.repository.BookingRepository;
import com.lrms.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional
public class BillingService {

    private final BillRepository billRepository;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;

    public BillingService(BillRepository billRepository, BookingRepository bookingRepository, OrderRepository orderRepository) {
        this.billRepository = billRepository;
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
    }

    public List<Bill> getAllBills() { return billRepository.findAll(); }

    /**
     * Returns a list of simplified bill summaries for the list view.
     * Resolves all entity relationships to avoid lazy loading issues in JSON.
     */
    public List<Map<String, Object>> getBillSummaryList() {
        List<Bill> bills = billRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Bill b : bills) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("billId", b.getBillId());
            // For dine-in walk-in: use customerName from the Order
            String guestName = "-";
            if (b.getGuest() != null) {
                guestName = b.getGuest().getFullName();
            } else if (b.getOrder() != null && b.getOrder().getCustomerName() != null) {
                guestName = b.getOrder().getCustomerName();
            }
            map.put("guestName", guestName);
            map.put("subtotal", b.getSubtotal());
            map.put("gstAmount", b.getGstAmount());
            map.put("totalAmount", b.getTotalAmount());
            map.put("paymentStatus", b.getPaymentStatus());
            map.put("paymentMode", b.getPaymentMode());
            map.put("createdAt", b.getCreatedAt());

            // Determine bill type label
            String billType = "-";
            if (b.getBooking() != null && b.getOrder() == null) {
                // Checkout bill (room only)
                billType = "Room " + b.getBooking().getRoom().getRoomNumber();
            } else if (b.getOrder() != null && b.getBooking() == null) {
                Order order = b.getOrder();
                if ("DINE_IN".equals(order.getOrderType()) && order.getTable() != null) {
                    billType = "Table " + order.getTable().getTableNumber() + " (Dine-in)";
                } else if ("ROOM_SERVICE".equals(order.getOrderType()) && order.getBooking() != null) {
                    billType = "Room " + order.getBooking().getRoom().getRoomNumber() + " (Room Service)";
                } else {
                    billType = "Order #" + order.getOrderId();
                }
            } else if (b.getBooking() != null && b.getOrder() != null) {
                billType = "Checkout";
            }
            map.put("billType", billType);
            result.add(map);
        }
        return result;
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Bill not found"));
    }

    public Bill generateBillForBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Booking not found"));
        
        // 1. Calculate Room Charges (12% GST)
        long days = booking.getCheckInDate().until(booking.getCheckOutDate()).getDays();
        if (days <= 0) days = 1;
        BigDecimal roomSubtotal = booking.getRoom().getRoomType().getBasePrice().multiply(new BigDecimal(days));

        // Store computed room charge on the booking
        booking.setTotalAmount(roomSubtotal);
        bookingRepository.save(booking);

        BigDecimal roomGst = roomSubtotal.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);

        // 2. Calculate Room Service Charges (5% GST)
        List<Order> orders = orderRepository.findByBookingBookingId(bookingId).stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .toList();
        
        BigDecimal fbSubtotal = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fbGst = fbSubtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);

        // 3. Totals
        BigDecimal totalSubtotal = roomSubtotal.add(fbSubtotal);
        BigDecimal totalGst = roomGst.add(fbGst);
        BigDecimal finalTotal = totalSubtotal.add(totalGst);

        Bill bill = new Bill();
        bill.setBooking(booking);
        bill.setGuest(booking.getGuest());
        bill.setSubtotal(totalSubtotal);
        bill.setGstAmount(totalGst);
        bill.setTotalAmount(finalTotal);
        
        return billRepository.save(bill);
    }

    public Bill generateBillForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Order not found"));
        
        BigDecimal subtotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal gst = subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(gst);

        Bill bill = new Bill();
        bill.setOrder(order);
        bill.setGuest(order.getGuest()); // null for walk-in dine-in customers
        bill.setSubtotal(subtotal);
        bill.setGstAmount(gst);
        bill.setTotalAmount(total);
        
        return billRepository.save(bill);
    }

    /**
     * Returns a detailed breakdown of a bill for display:
     * room info, room charges, food orders with line items, GST split, etc.
     */
    public Map<String, Object> getDetailedBill(Long billId) {
        Bill bill = getBillById(billId);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("billId", bill.getBillId());
        // Handle walk-in dine-in customers (no guest entity)
        if (bill.getGuest() != null) {
            detail.put("guestName", bill.getGuest().getFullName());
            detail.put("guestEmail", bill.getGuest().getEmail());
            detail.put("guestPhone", bill.getGuest().getPhone());
        } else if (bill.getOrder() != null && bill.getOrder().getCustomerName() != null) {
            detail.put("guestName", bill.getOrder().getCustomerName());
            detail.put("guestEmail", "-");
            detail.put("guestPhone", bill.getOrder().getCustomerPhone() != null ? bill.getOrder().getCustomerPhone() : "-");
        } else {
            detail.put("guestName", "Walk-in Customer");
            detail.put("guestEmail", "-");
            detail.put("guestPhone", "-");
        }
        detail.put("paymentStatus", bill.getPaymentStatus());
        detail.put("paymentMode", bill.getPaymentMode());
        detail.put("createdAt", bill.getCreatedAt());

        BigDecimal roomSubtotal = BigDecimal.ZERO;
        BigDecimal roomGst = BigDecimal.ZERO;
        BigDecimal fbSubtotal = BigDecimal.ZERO;
        BigDecimal fbGst = BigDecimal.ZERO;

        // Room charges breakdown
        if (bill.getBooking() != null) {
            Booking booking = bill.getBooking();
            Room room = booking.getRoom();
            long days = booking.getCheckInDate().until(booking.getCheckOutDate()).getDays();
            if (days <= 0) days = 1;
            BigDecimal basePrice = room.getRoomType().getBasePrice();
            roomSubtotal = basePrice.multiply(new BigDecimal(days));
            roomGst = roomSubtotal.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> roomInfo = new LinkedHashMap<>();
            roomInfo.put("roomNumber", room.getRoomNumber());
            roomInfo.put("roomType", room.getRoomType().getTypeName());
            roomInfo.put("floor", room.getFloor());
            roomInfo.put("checkInDate", booking.getCheckInDate().toString());
            roomInfo.put("checkOutDate", booking.getCheckOutDate().toString());
            roomInfo.put("nights", days);
            roomInfo.put("pricePerNight", basePrice);
            roomInfo.put("roomSubtotal", roomSubtotal);
            roomInfo.put("roomGst", roomGst);
            detail.put("roomCharges", roomInfo);

            // Room service orders
            List<Order> rsOrders = orderRepository.findByBookingBookingId(booking.getBookingId()).stream()
                    .filter(o -> !"CANCELLED".equals(o.getStatus()))
                    .toList();
            
            List<Map<String, Object>> orderList = new ArrayList<>();
            for (Order order : rsOrders) {
                Map<String, Object> orderMap = new LinkedHashMap<>();
                orderMap.put("orderId", order.getOrderId());
                orderMap.put("orderType", order.getOrderType());
                orderMap.put("status", order.getStatus());
                orderMap.put("placedAt", order.getPlacedAt());
                
                List<Map<String, Object>> itemList = new ArrayList<>();
                for (OrderItem item : order.getItems()) {
                    Map<String, Object> itemMap = new LinkedHashMap<>();
                    itemMap.put("itemName", item.getMenuItem() != null ? item.getMenuItem().getItemName() : "Unknown");
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("unitPrice", item.getUnitPrice());
                    itemMap.put("lineTotal", item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                    itemList.add(itemMap);
                }
                orderMap.put("items", itemList);
                orderMap.put("orderTotal", order.getTotalAmount());
                orderList.add(orderMap);
                
                fbSubtotal = fbSubtotal.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
            }
            fbGst = fbSubtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
            detail.put("foodOrders", orderList);
        }

        // If bill is for standalone dine-in order (no booking)
        if (bill.getOrder() != null && bill.getBooking() == null) {
            Order order = bill.getOrder();
            fbSubtotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            fbGst = fbSubtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);

            // Table info for dine-in orders
            if ("DINE_IN".equals(order.getOrderType()) && order.getTable() != null) {
                Map<String, Object> tableInfo = new LinkedHashMap<>();
                tableInfo.put("tableNumber", order.getTable().getTableNumber());
                tableInfo.put("section", order.getTable().getSection());
                tableInfo.put("capacity", order.getTable().getCapacity());
                detail.put("tableInfo", tableInfo);
            }

            // Room info for room service orders
            if ("ROOM_SERVICE".equals(order.getOrderType()) && order.getBooking() != null) {
                Map<String, Object> roomServiceInfo = new LinkedHashMap<>();
                roomServiceInfo.put("roomNumber", order.getBooking().getRoom().getRoomNumber());
                roomServiceInfo.put("roomType", order.getBooking().getRoom().getRoomType().getTypeName());
                roomServiceInfo.put("guestName", order.getBooking().getGuest().getFullName());
                detail.put("roomServiceInfo", roomServiceInfo);
            }

            List<Map<String, Object>> orderList = new ArrayList<>();
            Map<String, Object> orderMap = new LinkedHashMap<>();
            orderMap.put("orderId", order.getOrderId());
            orderMap.put("orderType", order.getOrderType());
            orderMap.put("status", order.getStatus());
            orderMap.put("placedAt", order.getPlacedAt());
            
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("itemName", item.getMenuItem() != null ? item.getMenuItem().getItemName() : "Unknown");
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unitPrice", item.getUnitPrice());
                itemMap.put("lineTotal", item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                itemList.add(itemMap);
            }
            orderMap.put("items", itemList);
            orderMap.put("orderTotal", order.getTotalAmount());
            orderList.add(orderMap);
            detail.put("foodOrders", orderList);
        }

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("roomSubtotal", roomSubtotal);
        summary.put("roomGst", roomGst);
        summary.put("foodSubtotal", fbSubtotal);
        summary.put("foodGst", fbGst);
        summary.put("subtotal", bill.getSubtotal());
        summary.put("totalGst", bill.getGstAmount());
        summary.put("discount", bill.getDiscount());
        summary.put("grandTotal", bill.getTotalAmount());
        detail.put("summary", summary);

        return detail;
    }

    public void payBill(Long billId, String paymentMode) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Bill not found"));
        bill.setPaymentMode(paymentMode);
        bill.setPaymentStatus("PAID");
        billRepository.save(bill);
    }
}

