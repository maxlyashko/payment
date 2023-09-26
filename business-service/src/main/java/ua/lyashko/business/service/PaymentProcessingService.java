package ua.lyashko.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.lyashko.commons.entity.RegularPaymentInstruction;
import ua.lyashko.commons.entity.Transaction;
import ua.lyashko.commons.enums.TransactionStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public void validatePayment(RegularPaymentInstruction paymentInstruction) {
        String payerINN = paymentInstruction.getPayerINN();
        if (payerINN==null || payerINN.length()!=10 || !payerINN.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid payer INN: " + payerINN);
        }
    }

    public void createPayment(RegularPaymentInstruction paymentInstruction) {
        Transaction transaction = createTransaction(paymentInstruction);
        ResponseEntity<Transaction> response = restTemplate.postForEntity(transactionBaseUrl, transaction, Transaction.class);
        handleResponse(response, "Creating payment");
    }

    public void processPayment(RegularPaymentInstruction paymentInstruction) {
        LocalDateTime lastTransactionDateTime = getLastTransactionDateTime(paymentInstruction.getId());
        int paymentPeriodMinutes = paymentInstruction.getPaymentPeriodMinutes();
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (isInstructionClosed(paymentInstruction)) {
            if (lastTransactionDateTime==null ||
                    currentDateTime.isAfter(lastTransactionDateTime.plusMinutes(paymentPeriodMinutes))) {
                List<Transaction> transactions = getTransactionsByPaymentInstruction(paymentInstruction);
                if (isInstructionClosed(paymentInstruction)) {
                    if (transactions.isEmpty() || isAnyTransactionActive(transactions)) {
                        Transaction transaction = createTransaction(paymentInstruction);
                        ResponseEntity<Transaction> response = restTemplate.postForEntity(transactionBaseUrl, transaction, Transaction.class);
                        handleResponse(response, "Processing payment");
                    }
                }
            }
        }
    }


    private boolean isAnyTransactionActive(List<Transaction> transactions) {
        return transactions.stream().noneMatch(transaction -> transaction.getTransactionStatus()==TransactionStatus.ACTIVE);
    }

    private boolean isInstructionClosed(RegularPaymentInstruction paymentInstruction) {
        List<Transaction> transactions = getTransactionsByPaymentInstruction(paymentInstruction);
        if (transactions.isEmpty()) {
            return true;
        }
        boolean allTransactionsStorno = transactions.stream()
                .allMatch(transaction -> transaction.getTransactionStatus()==TransactionStatus.STORNO);
        return !allTransactionsStorno && isAnyTransactionActive(transactions);
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
        return transactions.stream()
                .map(Transaction::getTransactionDateTime)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
    }

    public List<RegularPaymentInstruction> getInstructionsForProcessing() {
        ResponseEntity<RegularPaymentInstruction[]> response = restTemplate.getForEntity(paymentInstructionBaseUrl, RegularPaymentInstruction[].class);
        if (response.getStatusCode().is2xxSuccessful()) {
            RegularPaymentInstruction[] allPaymentInstructions = response.getBody();
            LocalDateTime currentDateTime = LocalDateTime.now();
            List<RegularPaymentInstruction> instructionsToProcess = new ArrayList<>();
            if (allPaymentInstructions!=null) {
                instructionsToProcess = Arrays.stream(allPaymentInstructions)
                        .filter(paymentInstruction -> shouldProcessPayment(paymentInstruction, currentDateTime))
                        .collect(Collectors.toList());
            }
            return instructionsToProcess;
        } else {
            return Collections.emptyList();
        }
    }

    private boolean shouldProcessPayment(RegularPaymentInstruction paymentInstruction, LocalDateTime currentDateTime) {
        LocalDateTime lastTransactionDateTime = getLastTransactionDateTime(paymentInstruction.getId());
        int paymentPeriodMinutes = paymentInstruction.getPaymentPeriodMinutes();

        return lastTransactionDateTime==null ||
                currentDateTime.isAfter(lastTransactionDateTime.plusMinutes(paymentPeriodMinutes));
    }

    private Transaction createTransaction(RegularPaymentInstruction paymentInstruction) {
        Transaction transaction = new Transaction();
        transaction.setPaymentInstruction(paymentInstruction);
        transaction.setTransactionAmount(paymentInstruction.getPaymentAmount());
        transaction.setTransactionStatus(TransactionStatus.ACTIVE);
        transaction.setTransactionDateTime(LocalDateTime.now());
        return transaction;
    }

    private void handleResponse(ResponseEntity<?> response, String action) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to perform " + action);
        }
    }

    private List<Transaction> getTransactionsByPaymentInstruction(RegularPaymentInstruction paymentInstruction) {
        ResponseEntity<Transaction[]> response = restTemplate.getForEntity(transactionBaseUrl + "/by-payment-instruction/" + paymentInstruction.getId(), Transaction[].class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return List.of(Objects.requireNonNull(response.getBody()));
        } else {
            return new java.util.ArrayList<>();
        }
    }
}
