package com.lrms.controller;

import com.lrms.entity.Bill;
import com.lrms.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "Billing", description = "Invoicing and Payment management")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public List<Map<String, Object>> getAllBills() { return billingService.getBillSummaryList(); }

    @GetMapping("/{id}")
    public Map<String, Object> getBillDetail(@PathVariable Long id) {
        return billingService.getDetailedBill(id);
    }

    @PostMapping("/generate")
    public Bill generateBill(@RequestBody Map<String, Long> params) {
        if (params.containsKey("bookingId")) {
            return billingService.generateBillForBooking(params.get("bookingId"));
        } else if (params.containsKey("orderId")) {
            return billingService.generateBillForOrder(params.get("orderId"));
        }
        throw new IllegalArgumentException("bookingId or orderId required");
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<?> payBill(@PathVariable Long id, @RequestBody Map<String, String> body) {
        billingService.payBill(id, body.get("paymentMode"));
        return ResponseEntity.ok().build();
    }
}

