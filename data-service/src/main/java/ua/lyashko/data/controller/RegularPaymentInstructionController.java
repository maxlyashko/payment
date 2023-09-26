package ua.lyashko.data.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.lyashko.commons.entity.RegularPaymentInstruction;
import ua.lyashko.data.service.RegularPaymentInstructionService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payment-instructions")
public class RegularPaymentInstructionController {
    private final RegularPaymentInstructionService instructionService;

    @Autowired
    public RegularPaymentInstructionController(RegularPaymentInstructionService instructionService) {
        this.instructionService = instructionService;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody RegularPaymentInstruction instruction) {
        return ResponseEntity.created(
                URI.create("/api/payment-instructions/" + instructionService.create(instruction).getId())).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegularPaymentInstruction> getById(@PathVariable Long id) {
        return instructionService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping()
    public ResponseEntity<List<RegularPaymentInstruction>> getAll() {
        return ResponseEntity.ok(instructionService.getAllPaymentInstructions());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegularPaymentInstruction> update(@PathVariable Long id, @RequestBody RegularPaymentInstruction instruction) {
        return instructionService.update(id, instruction)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        instructionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-payer-inn/{payerINN}")
    public ResponseEntity<List<RegularPaymentInstruction>> getByPayerINN(@PathVariable String payerINN) {
        return ResponseEntity.ok(instructionService.getByPayerINN(payerINN));
    }

    @GetMapping("/by-payee-okpo/{payeeOKPO}")
    public ResponseEntity<List<RegularPaymentInstruction>> getByPayeeOKPO(@PathVariable String payeeOKPO) {
        return ResponseEntity.ok(instructionService.getByPayeeOKPO(payeeOKPO));
    }
}
