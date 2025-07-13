package org.example.dao;

import org.example.constants.LoanStatus;
import org.example.model.Loan;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.util.List;

public interface LoanDAO{

    List<Loan> getLoansByCreditorId(int creditorId);

    List<Loan> getLoanByDebtorIdAndStatus(int debtorId, LoanStatus loanStatus);

    BigDecimal sumOfLoansByDebtor(int debtorId, LoanStatus loanStatus);

    BigDecimal avgOfInterestRateByLastMonth();

    List<Loan> almostExpiredLoans();

    List<Loan> getAllLoansByStatus(LoanStatus loanStatus);

    Loan getPaymentsByLoan(int loanId);
}
