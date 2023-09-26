package ua.lyashko.commons.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ua.lyashko.commons.enums.TransactionStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_date_time")
    private LocalDateTime transactionDateTime;

    @ManyToOne
    private RegularPaymentInstruction paymentInstruction;

    @Column(name = "transaction_amount")
    private double transactionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status")
    private TransactionStatus transactionStatus;
}
