package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.constants.LoanStatus;
import org.example.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LoanDTO {

    private User creditor;
    private User debtor;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private int term;

    public boolean isValid() {
        return creditor != null
                && debtor != null
                && amount != null && amount.compareTo(BigDecimal.ZERO) > 0
                && interestRate != null && interestRate.compareTo(BigDecimal.ZERO) > 0
                && term > 0 && term <= 12;
    }
}
