package ua.lyashko.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.lyashko.commons.entity.RegularPaymentInstruction;
import ua.lyashko.commons.entity.Transaction;
import ua.lyashko.data.repository.TransactionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction create(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> update(Long id, Transaction updatedTransaction) {
        return getById(id).map(existingTransaction -> {
            if (updatedTransaction.getTransactionAmount()!=0.0) {
                existingTransaction.setTransactionAmount(updatedTransaction.getTransactionAmount());
            }
            if (updatedTransaction.getTransactionStatus()!=null) {
                existingTransaction.setTransactionStatus(updatedTransaction.getTransactionStatus());
            }
            if (updatedTransaction.getTransactionDateTime()!=null) {
                existingTransaction.setTransactionDateTime(updatedTransaction.getTransactionDateTime());
            }
            if (updatedTransaction.getPaymentInstruction()!=null) {
                existingTransaction.setPaymentInstruction(updatedTransaction.getPaymentInstruction());
            }
            return transactionRepository.save(existingTransaction);
        });
    }

    public void delete(Long id) {
        transactionRepository.deleteById(id);
    }

    public Optional<Transaction> getById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getByPaymentInstruction(RegularPaymentInstruction paymentInstruction) {
        return transactionRepository.findByPaymentInstruction(paymentInstruction);
    }
}
