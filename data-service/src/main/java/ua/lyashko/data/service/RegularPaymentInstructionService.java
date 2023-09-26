package ua.lyashko.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.lyashko.commons.entity.RegularPaymentInstruction;
import ua.lyashko.data.repository.RegularPaymentInstructionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RegularPaymentInstructionService {
    private final RegularPaymentInstructionRepository instructionRepository;

    @Autowired
    public RegularPaymentInstructionService(RegularPaymentInstructionRepository instructionRepository) {
        this.instructionRepository = instructionRepository;
    }

    public RegularPaymentInstruction create(RegularPaymentInstruction instruction) {
        return instructionRepository.save(instruction);
    }

    public Optional<RegularPaymentInstruction> update(Long id, RegularPaymentInstruction updatedInstruction) {
        return getById(id).map(existingInstruction -> {
            if (updatedInstruction.getPayerName()!=null) {
                existingInstruction.setPayerName(updatedInstruction.getPayerName());
            }
            if (updatedInstruction.getPayerINN()!=null) {
                existingInstruction.setPayerINN(updatedInstruction.getPayerINN());
            }
            if (updatedInstruction.getPayerCardNumber()!=null) {
                existingInstruction.setPayerCardNumber(updatedInstruction.getPayerCardNumber());
            }
            if (updatedInstruction.getPayeeAccount()!=null) {
                existingInstruction.setPayeeAccount(updatedInstruction.getPayeeAccount());
            }
            if (updatedInstruction.getPayeeMFO()!=null) {
                existingInstruction.setPayeeMFO(updatedInstruction.getPayeeMFO());
            }
            if (updatedInstruction.getPayeeOKPO()!=null) {
                existingInstruction.setPayeeOKPO(updatedInstruction.getPayeeOKPO());
            }
            if (updatedInstruction.getPayeeName()!=null) {
                existingInstruction.setPayeeName(updatedInstruction.getPayeeName());
            }
            if (updatedInstruction.getPaymentPeriodMinutes()!=0) {
                existingInstruction.setPaymentPeriodMinutes(updatedInstruction.getPaymentPeriodMinutes());
            }
            if (updatedInstruction.getPaymentAmount()!=0.0) {
                existingInstruction.setPaymentAmount(updatedInstruction.getPaymentAmount());
            }
            return instructionRepository.save(existingInstruction);
        });
    }


    public void delete(Long id) {
        instructionRepository.deleteById(id);
    }

    public List<RegularPaymentInstruction> getAllPaymentInstructions() {
        return (List<RegularPaymentInstruction>) instructionRepository.findAll();
    }

    public Optional<RegularPaymentInstruction> getById(Long id) {
        return instructionRepository.findById(id);
    }

    public List<RegularPaymentInstruction> getByPayerINN(String payerINN) {
        return instructionRepository.findByPayerINN(payerINN);
    }

    public List<RegularPaymentInstruction> getByPayeeOKPO(String payeeOKPO) {
        return instructionRepository.findByPayeeOKPO(payeeOKPO);
    }
}
