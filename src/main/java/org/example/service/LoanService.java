package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.constants.LoanStatus;
import org.example.constants.PaymentType;
import org.example.dao.LoanDAO;
import org.example.dto.LoanDTO;
import org.example.model.Loan;
import org.example.model.Payment;
import org.example.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
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

    public Loan createLoan(LoanDTO loanDTO, PaymentService paymentService) {
        if (!loanDTO.isValid()) {
            throw new IllegalArgumentException("Invalid loan creation data");
        }
        if (!canDebtorTakeNewLoan(loanDTO.getDebtor())) {
            throw new IllegalStateException("Debtor cannot take new loan");
        }
        Loan loan = Loan.builder()
                .amount(loanDTO.getAmount())
                .creditor(loanDTO.getCreditor())
                .debtor(loanDTO.getDebtor())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(loanDTO.getTerm()))
                .interestRate(loanDTO.getInterestRate())
                .status(LoanStatus.ACTIVE)
                .payments(new ArrayList<>())
                .build();
        loanDAO.save(loan);

        List<Payment> schedule = paymentService.generateAndSaveSchedule(loan);
        loan.setPayments(schedule);

        return loan;
    }

    public void updateLoanStatus(Loan loan, LoanStatus loanStatus) {
        if (loan == null) {
            throw new IllegalArgumentException("Update Loan: Invalid loan");
        }
        loan.setStatus(loanStatus);
        if (loan.getStatus().equals(LoanStatus.REPAID)) {
            loan.setEndDate(LocalDate.now());
            logger.debug("Status and expired date were changed!");
        }
        loanDAO.update(loan);
        logger.debug("Status was changed!");
    }

    public void closeLoan(Loan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("Close Loan: Loan not found");
        }
        List<Payment> payments = loan.getPayments();
        for (Payment payment : payments) {
            if ((payment.getType().equals(PaymentType.PENDING))) {
                logger.warn("Closing loan is impossible, because it has unpaid payment");
                throw new IllegalStateException("Cannot close loan with pending payments");
            }
        }
        updateLoanStatus(loan, LoanStatus.REPAID);
    }

    public List<Loan> getActiveLoansForCreditorInThisYear(User creditor) {
        if (creditor == null) {
            throw new IllegalArgumentException("Active Loans In Year: Creditor cannot be null");
        }
        List<Loan> loansByCreditorId = loanDAO.getLoansByCreditorId(creditor.getId());
        LocalDate startOfYear = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
        return loansByCreditorId.stream()
                .filter(loan -> loan.getStatus().equals(LoanStatus.ACTIVE) && loan.getStartDate().isAfter(startOfYear))
                .collect(Collectors.toList());
    }

    public List<Loan> getActiveLoansForDebtor(User debtor) {
        if (debtor == null) {
            throw new IllegalArgumentException("Active Loans In Year: Debtor cannot be null");
        }
        return loanDAO.getLoanByDebtorIdAndStatus(debtor.getId(), LoanStatus.ACTIVE);
    }

    public BigDecimal calculateRemainingDebt(Loan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("Calculate Remaining: Loan not found");
        }
        return loan.getPayments().stream()
                .filter(payment -> payment.getType() == PaymentType.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal processInterestAccrual(Loan loan, LocalDate startDate, LocalDate endDate, BigDecimal principal) {
        if (startDate.isAfter(endDate) || startDate.equals(endDate)) {
            return BigDecimal.ZERO;
        }
        if (principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        long daysSinceLastPayment = ChronoUnit.DAYS.between(startDate, endDate);
        return principal
                .multiply(loan.getInterestRate())
                .multiply(BigDecimal.valueOf(daysSinceLastPayment))
                .divide(BigDecimal.valueOf(365), RoundingMode.HALF_UP);
    }

    public boolean checkOverdueLoans() {
        List<Loan> activeLoans = loanDAO.getAllLoansByStatus(LoanStatus.ACTIVE);
        for (Loan loan : activeLoans) {
            if (isOverdue(loan)) {
                updateLoanStatus(loan, LoanStatus.DEFAULTED);
                loanDAO.update(loan);
                return true;
            }
        }
        return false;
    }

    public boolean canDebtorTakeNewLoan(User debtor) {
        if (debtor == null) {
            throw new IllegalArgumentException("Can Debtor Take New Loan: Debtor cannot be null");
        }
        BigDecimal sumOfActiveLoans = getTotalDebtByDebtor(debtor);
        if (sumOfActiveLoans.compareTo(BigDecimal.valueOf(50_000)) >= 0) {
            return false;
        } else if (!(loanDAO.getLoanByDebtorIdAndStatus(debtor.getId(), LoanStatus.DEFAULTED).isEmpty())) {
            return false;
        } else {
            return true;
        }
    }

    public List<Loan> getUpcomingExpirations() {
        return loanDAO.almostExpiredLoans().stream()
                .sorted(Comparator.comparing(Loan::getEndDate))
                .toList();
    }

    public List<Loan> getAllLoans() {
        return loanDAO.findAll();
    }


    private BigDecimal getTotalDebtByDebtor(User debtor) {
        return loanDAO.sumOfLoansByDebtor(debtor.getId(), LoanStatus.ACTIVE);
    }

    private boolean isOverdue(Loan loan) {
        return loan.getEndDate().isBefore(LocalDate.now());
    }
}
