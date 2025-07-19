package org.example.dao;

import org.example.constants.LoanStatus;
import org.example.model.Loan;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface LoanDAO extends BaseMethodsDAO<Loan>{

    List<Loan> getLoansByCreditorId(Long creditorId);

    List<Loan> getLoanByDebtorIdAndStatus(Long debtorId, LoanStatus loanStatus);

    BigDecimal sumOfLoansByDebtor(Long debtorId, LoanStatus loanStatus);

    BigDecimal avgOfInterestRate(Collection<LoanStatus> loanStatuses);

    List<Loan> almostExpiredLoans();

    List<Loan> getAllLoansByStatus(LoanStatus loanStatus);

    Loan getPaymentsByLoan(Long loanId);
}
