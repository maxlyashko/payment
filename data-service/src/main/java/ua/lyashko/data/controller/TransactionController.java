package ua.lyashko.data.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ua.lyashko.commons.entity.RegularPaymentInstruction;
import ua.lyashko.commons.entity.Transaction;
import ua.lyashko.data.service.RegularPaymentInstructionService;
import ua.lyashko.data.service.TransactionService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final RegularPaymentInstructionService instructionService;

    @Autowired
    public TransactionController(TransactionService transactionService, RegularPaymentInstructionService instructionService) {
        this.transactionService = transactionService;
        this.instructionService = instructionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction) {
        return ResponseEntity.created(
                URI.create("/api/transactions/" + transactionService.create(transaction).getId())).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable Long id) {
        return transactionService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Long id, @RequestBody Transaction transaction) {
        return transactionService.update(id,transaction)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-payment-instruction/{instructionId}")
    public ResponseEntity<List<Transaction>> getByPaymentInstruction(@PathVariable Long instructionId) {
        Optional<RegularPaymentInstruction> instruction = instructionService.getById(instructionId);

        if (instruction.isPresent()) {
            List<Transaction> transactions = transactionService.getByPaymentInstruction(instruction.get());
            return ResponseEntity.ok(transactions);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
