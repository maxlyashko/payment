package ua.lyashko.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.lyashko.commons.entity.RegularPaymentInstruction;

import java.util.List;

@Repository
public interface RegularPaymentInstructionRepository extends CrudRepository<RegularPaymentInstruction, Long> {
    List<RegularPaymentInstruction> findByPayerINN(String payerINN);

    List<RegularPaymentInstruction> findByPayeeOKPO(String payeeOKPO);
}
