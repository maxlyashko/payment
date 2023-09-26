package ua.lyashko.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.lyashko.commons.entity.RegularPaymentInstruction;
import ua.lyashko.commons.entity.Transaction;
import ua.lyashko.commons.enums.TransactionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class PaymentProcessingService {
    private final RestTemplate restTemplate;
    private final String paymentInstructionBaseUrl;
    private final String transactionBaseUrl;

    @Autowired
    public PaymentProcessingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.paymentInstructionBaseUrl = "http://localhost:8081/api/payment-instructions";
        this.transactionBaseUrl = "http://localhost:8081/api/transactions";
    }

    public void validatePayment(RegularPaymentInstruction paymentInstruction) throws IllegalArgumentException {
        String payerINN = paymentInstruction.getPayerINN();
        if (payerINN==null || payerINN.length()!=10 || !payerINN.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid payer INN");
        }
    }

    public void createPayment(RegularPaymentInstruction paymentInstruction) {
        Transaction transaction = new Transaction();
        transaction.setPaymentInstruction(paymentInstruction);
        transaction.setTransactionAmount(paymentInstruction.getPaymentAmount());
        transaction.setTransactionStatus(TransactionStatus.ACTIVE);
        transaction.setTransactionDateTime(LocalDateTime.now());

        ResponseEntity<Transaction> response = restTemplate.postForEntity(transactionBaseUrl, transaction, Transaction.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("invalid creating payment");
        }
    }

    public void processPayment(RegularPaymentInstruction paymentInstruction) {
        LocalDateTime lastTransactionDateTime = getLastTransactionDateTime(paymentInstruction.getId());
        int paymentPeriodMinutes = paymentInstruction.getPaymentPeriodMinutes();
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (lastTransactionDateTime==null ||
                currentDateTime.isAfter(lastTransactionDateTime.plusMinutes(paymentPeriodMinutes))) {
            Transaction transaction = new Transaction();
            transaction.setPaymentInstruction(paymentInstruction);
            transaction.setTransactionAmount(paymentInstruction.getPaymentAmount());
            transaction.setTransactionStatus(TransactionStatus.ACTIVE);
            transaction.setTransactionDateTime(currentDateTime);

            ResponseEntity<Transaction> response = restTemplate.postForEntity(transactionBaseUrl, transaction, Transaction.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("invalid processing");
            }
        }
    }

    public void cancelPayment(RegularPaymentInstruction paymentInstruction) {
        List<Transaction> transactions = getTransactionsByPaymentInstruction(paymentInstruction);
        for (Transaction transaction : transactions) {
            transaction.setTransactionStatus(TransactionStatus.STORNO);
            restTemplate.put(transactionBaseUrl + "/" + transaction.getId(), transaction);
        }
    }

    public LocalDateTime getLastTransactionDateTime(Long paymentId) {
        RegularPaymentInstruction paymentInstruction = restTemplate.getForObject(paymentInstructionBaseUrl + "/" + paymentId, RegularPaymentInstruction.class);
        assert paymentInstruction!=null;
        List<Transaction> transactions = getTransactionsByPaymentInstruction(paymentInstruction);
        LocalDateTime lastTransactionDateTime = LocalDateTime.MIN;
        for (Transaction transaction : transactions) {
            if (transaction.getTransactionDateTime().isAfter(lastTransactionDateTime)) {
                lastTransactionDateTime = transaction.getTransactionDateTime();
            }
        }
        return lastTransactionDateTime;
    }

    public List<RegularPaymentInstruction> getInstructionsForProcessing() {
        ResponseEntity<RegularPaymentInstruction[]> response = restTemplate.getForEntity(paymentInstructionBaseUrl, RegularPaymentInstruction[].class);
        if (response.getStatusCode().is2xxSuccessful()) {
            RegularPaymentInstruction[] allPaymentInstructions = response.getBody();
            List<RegularPaymentInstruction> instructionsToProcess = new ArrayList<>();

            LocalDateTime currentDateTime = LocalDateTime.now();

            assert allPaymentInstructions!=null;
            for (RegularPaymentInstruction paymentInstruction : allPaymentInstructions) {
                LocalDateTime lastTransactionDateTime = getLastTransactionDateTime(paymentInstruction.getId());
                int paymentPeriodMinutes = paymentInstruction.getPaymentPeriodMinutes();

                if (lastTransactionDateTime==null ||
                        currentDateTime.isAfter(lastTransactionDateTime.plusMinutes(paymentPeriodMinutes))) {
                    instructionsToProcess.add(paymentInstruction);
                }
            }

            return instructionsToProcess;
        } else {
            return new ArrayList<>();
        }
    }

    private List<Transaction> getTransactionsByPaymentInstruction(RegularPaymentInstruction paymentInstruction) {
        ResponseEntity<Transaction[]> response = restTemplate.getForEntity(transactionBaseUrl + "/by-payment-instruction/" + paymentInstruction.getId(), Transaction[].class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return List.of(Objects.requireNonNull(response.getBody()));
        } else {
            return new ArrayList<>();
        }
    }
}
