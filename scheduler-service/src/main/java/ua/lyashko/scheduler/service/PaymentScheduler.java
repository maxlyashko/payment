package ua.lyashko.scheduler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ua.lyashko.commons.entity.RegularPaymentInstruction;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class PaymentScheduler {
    private final RestTemplate restTemplate;
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8080/api/business/payments";

    @Autowired
    public PaymentScheduler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 60000)
    public void processScheduledPayments() {
        List<RegularPaymentInstruction> paymentsToProcess = getInstructionsForProcessing();
        LocalDateTime currentDateTime = LocalDateTime.now();

        for (RegularPaymentInstruction payment : paymentsToProcess) {
            LocalDateTime lastTransactionDateTime = getLastTransactionDateTime(payment);
            int paymentPeriodMinutes = payment.getPaymentPeriodMinutes();

            if (lastTransactionDateTime==null ||
                    currentDateTime.isAfter(lastTransactionDateTime.plusMinutes(paymentPeriodMinutes))) {
                createPayment(payment);
            }
        }
    }

    private List<RegularPaymentInstruction> getInstructionsForProcessing() {
        ResponseEntity<RegularPaymentInstruction[]> response = restTemplate.getForEntity(
                PAYMENT_SERVICE_URL + "/instructions-for-processing", RegularPaymentInstruction[].class);
        RegularPaymentInstruction[] regularPaymentInstructions = response.getBody();
        return Arrays.asList(Objects.requireNonNull(regularPaymentInstructions));
    }

    private LocalDateTime getLastTransactionDateTime(RegularPaymentInstruction payment) {
        ResponseEntity<LocalDateTime> response = restTemplate.getForEntity(
                PAYMENT_SERVICE_URL + "/last-transaction-datetime/{id}", LocalDateTime.class, payment.getId());
        return response.getBody();
    }

    private void createPayment(RegularPaymentInstruction payment) {
        restTemplate.postForEntity(PAYMENT_SERVICE_URL + "/create", payment, Void.class);
    }
}
