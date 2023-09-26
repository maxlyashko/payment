package ua.lyashko.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.lyashko.business.service.PaymentProcessingService;
import ua.lyashko.commons.entity.RegularPaymentInstruction;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/business/payments")
public class PaymentProcessingController {
    private final PaymentProcessingService paymentProcessingService;

    @Autowired
    public PaymentProcessingController(PaymentProcessingService paymentProcessingService) {
        this.paymentProcessingService = paymentProcessingService;
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validatePayment(@RequestBody RegularPaymentInstruction paymentInstruction) {
        try {
            paymentProcessingService.validatePayment(paymentInstruction);
            return ResponseEntity.ok("Payment validation successful");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Payment validation failed: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createPayment(@RequestBody RegularPaymentInstruction paymentInstruction) {
        paymentProcessingService.createPayment(paymentInstruction);
        return ResponseEntity.ok("Payment created successfully");
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestBody RegularPaymentInstruction paymentInstruction) {
        paymentProcessingService.processPayment(paymentInstruction);
        return ResponseEntity.ok("Payment processed successfully");
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelPayment(@RequestBody RegularPaymentInstruction paymentInstruction) {
        paymentProcessingService.cancelPayment(paymentInstruction);
        return ResponseEntity.ok("Payment canceled successfully");
    }

    @GetMapping("/instructions-for-processing")
    public ResponseEntity<List<RegularPaymentInstruction>> getInstructionsForProcessing() {
        List<RegularPaymentInstruction> instructions = paymentProcessingService.getInstructionsForProcessing();
        return ResponseEntity.ok(instructions);
    }

    @GetMapping("/last-transaction-datetime/{id}")
    public ResponseEntity<LocalDateTime> getLastTransactionDateTime(@PathVariable Long id) {
        LocalDateTime lastTransactionDateTime = paymentProcessingService.getLastTransactionDateTime(id);
        return ResponseEntity.ok(lastTransactionDateTime);
    }
}
