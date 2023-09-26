package ua.lyashko.commons.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RegularPaymentInstruction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payer_name")
    private String payerName;

    @Column(name = "payer_inn", length = 10)
    private String payerINN;

    @Column(name = "payer_card_number", length = 16)
    private String payerCardNumber;

    @Column(name = "payee_account", length = 29)
    private String payeeAccount;

    @Column(name = "payee_mfo", length = 6)
    private String payeeMFO;

    @Column(name = "payee_okpo", length = 8)
    private String payeeOKPO;

    @Column(name = "payee_name")
    private String payeeName;

    @Column(name = "payment_period_minutes")
    private int paymentPeriodMinutes;

    @Column(name = "payment_amount")
    private double paymentAmount;
}
