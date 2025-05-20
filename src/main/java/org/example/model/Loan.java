package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "loan", schema = "public")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;
    @ManyToOne
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;
    private BigDecimal amount;
    @Column(name = "interest_rate")
    private BigDecimal interestRate;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

}
