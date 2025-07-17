package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.constants.LoanStatus;
import org.example.constants.PaymentType;
import org.example.dao.LoanDAO;
import org.example.model.Loan;
import org.example.model.Payment;
import org.example.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class LoanService {

    private final LoanDAO loanDAO;

    public LoanService(LoanDAO loanDAO) {
        this.loanDAO = loanDAO;
    }

    private boolean validateData(User creditor, User debtor, BigDecimal amount, BigDecimal interestRate, int term) {
        if (creditor == null || debtor == null) {
            return false;
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        } else if (term <= 0 || term > 12) {
            return false;
        } else if (interestRate.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        } else {
            return true;
        }
    }

    public Loan createLoan(User creditor, User debtor, BigDecimal amount, BigDecimal interestRate, int term) {
        if (!(validateData(creditor, debtor, amount, interestRate, term))) {
            throw new IllegalArgumentException();
        }
        if (!canDebtorTakeNewLoan(debtor)) {
            logger.warn("Taking out of new loan is forbidden: debtor have a defaulted loans.");
            throw new IllegalStateException("Debtor cannot take new loan");
        }
        Loan loan = Loan.builder()
                .amount(amount)
                .creditor(creditor)
                .debtor(debtor)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(term))
                .interestRate(interestRate)
                .status(LoanStatus.ACTIVE)
                .payments(new ArrayList<>())
                .build();
        loanDAO.saveOrUpdate(loan);

        // звернутися до методу з PaymentService для створення графіку платежів
        return loan;
    }

    public void updateLoanStatus(Loan loan, LoanStatus loanStatus) {
        loan.setStatus(loanStatus);
        if (loan.getStatus().equals(LoanStatus.REPAID)) {
            loan.setEndDate(LocalDate.now());
            logger.debug("Status and expired date were changed!");
        }
        loanDAO.saveOrUpdate(loan);
        logger.debug("Status was changed!");
    }

    public void closeLoan(Loan loan) {
        List<Payment> payments = loan.getPayments();
        for (Payment payment : payments) {
            if ((payment.getType().equals(PaymentType.PENDING))) {
                logger.warn("Closing loan is impossible, because it has unpaid payment");
                throw new IllegalStateException("Cannot close loan with pending payments");
            }
        }
        updateLoanStatus(loan, LoanStatus.REPAID);
    }

    public Loan getLoanById(int id) {
        return loanDAO.findById(id);
    }

    public Loan getPaymentsAndLoan(Loan loan) {
        return loanDAO.getPaymentsByLoan(loan.getId());
    }

    public List<Loan> getActiveLoansForCreditorInThisYear(User creditor) {
        List<Loan> loansByCreditorId = loanDAO.getLoansByCreditorId(creditor.getId());
        LocalDate year = LocalDate.now().minusMonths(12);
        return loansByCreditorId.stream()
                .filter(loan -> loan.getStatus().equals(LoanStatus.ACTIVE) && loan.getStartDate().isAfter(year))
                .collect(Collectors.toList());
    }

    public List<Loan> getActiveLoansForDebtor(User debtor) {
        return loanDAO.getLoanByDebtorIdAndStatus(debtor.getId(), LoanStatus.ACTIVE);
    }

    public BigDecimal calculateRemainingDebt(Loan loan) {
        long numberOfDays = ChronoUnit.DAYS.between(loan.getStartDate(), LocalDate.now());
        List<Payment> payments = loan.getPayments();
        BigDecimal sumOfPayments = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal percents = loan.getAmount().multiply(loan.getInterestRate()).multiply(BigDecimal.valueOf(numberOfDays))
                .divide(BigDecimal.valueOf(365), RoundingMode.HALF_UP);
        return loan.getAmount().add(percents).subtract(sumOfPayments);
    }

    public void checkOverdueLoans() {
        List<Loan> activeLoans = loanDAO.getAllLoansByStatus(LoanStatus.ACTIVE);
        for (Loan loan : activeLoans) {
            if (isOverdue(loan)) {
                loan.setStatus(LoanStatus.DEFAULTED);
                loanDAO.saveOrUpdate(loan);
                logger.warn("Status of loan was changed: DEFAULTED!");
            }
        }
    }

    public void processInterestAccrual() {
        List<Loan> activeLoans = loanDAO.getAllLoansByStatus(LoanStatus.ACTIVE);
        for (Loan loan : activeLoans) {
            loan.getPayments();
        }
        // потрібно організувати метод для суми внесених платежів у PaymentService
    }

    public boolean canDebtorTakeNewLoan(User debtor) {
        BigDecimal sumOfActiveLoans = getTotalDebtByDebtor(debtor);
        if (sumOfActiveLoans.compareTo(BigDecimal.valueOf(50_000)) > 0) {
            logger.warn("Taking out of new loan is forbidden: loan limit has been reached.");
            return false;
        } else if (!(loanDAO.getLoanByDebtorIdAndStatus(debtor.getId(), LoanStatus.DEFAULTED).isEmpty())) {
            return false;
        } else {
            return true;
        }
    }

    public BigDecimal getAverageInterestRateForAllLoans() {
        return loanDAO.avgOfInterestRate(Arrays.asList(LoanStatus.ACTIVE, LoanStatus.REPAID, LoanStatus.DEFAULTED));
    }

    public BigDecimal getAverageInterestRateForActiveLoans() {
        return loanDAO.avgOfInterestRate(List.of(LoanStatus.ACTIVE));
    }

    public List<Loan> getUpcomingExpirations() {
        return loanDAO.almostExpiredLoans().stream()
                .sorted(Comparator.comparing(Loan::getEndDate))
                .toList();
    }


    public BigDecimal getTotalDebtByDebtor(User debtor) {
        return loanDAO.sumOfLoansByDebtor(debtor.getId(), LoanStatus.ACTIVE);
    }

    private boolean isOverdue(Loan loan) {
        if (loan.getEndDate().isBefore(LocalDate.now())) {
            return true;
        }
        return false;
    }
}
