package ua.lyashko.data.repository;

import org.springframework.data.repository.CrudRepository;
import ua.lyashko.commons.entity.RegularPaymentInstruction;
import ua.lyashko.commons.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    List<Transaction> findByPaymentInstruction(RegularPaymentInstruction paymentInstruction);
}
