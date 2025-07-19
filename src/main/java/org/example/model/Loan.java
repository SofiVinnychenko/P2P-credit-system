package org.example.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.constants.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "loans", schema = "public")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;
    private BigDecimal amount;
    @Column(name = "interest_rate")
    private BigDecimal interestRate;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    @Setter
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LoanStatus status;
    @Setter
    @OneToMany(mappedBy = "loan", fetch = FetchType.LAZY)
    private List<Payment> payments;

    public void setStatus(LoanStatus status) {
        if (this.status == LoanStatus.REPAID) {
            throw new IllegalArgumentException();
        }
        this.status = status;
    }
}
