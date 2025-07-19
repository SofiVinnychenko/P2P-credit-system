package org.example.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.constants.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "payments", schema = "public")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    private LocalDate dueDate;
    @Setter
    private LocalDate paidDate;
    private BigDecimal amount;
    @Setter
    @Enumerated(EnumType.STRING)
    private PaymentType type;
}
