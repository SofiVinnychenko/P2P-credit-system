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
        loan.setStatus(loanStatus);
        if (loan.getStatus().equals(LoanStatus.REPAID)) {
            loan.setEndDate(LocalDate.now());
            logger.debug("Status and expired date were changed!");
        }
        loanDAO.update(loan);
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

    public Loan getLoanById(Long id) {
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

        BigDecimal currentPrincipal = loan.getAmount();
        LocalDate lastCalculationDate = loan.getStartDate();

        List<Payment> sortedPayments = loan.getPayments().stream()
                .filter(payment -> payment.getType().equals(PaymentType.PAID))
                .sorted(Comparator.comparing(Payment::getPaidDate))
                .toList();

        for (Payment payment : sortedPayments) {

            BigDecimal percents = processInterestAccrual(loan, lastCalculationDate, payment.getPaidDate(), currentPrincipal);

            lastCalculationDate = payment.getPaidDate();

            BigDecimal paymentAmount = payment.getAmount();
            if (paymentAmount.compareTo(percents) >= 0) {
                BigDecimal principalPaid = paymentAmount.subtract(percents);
                currentPrincipal = currentPrincipal.subtract(principalPaid);
            } else {
                BigDecimal unpaidInterest = percents.subtract(paymentAmount);
                currentPrincipal = currentPrincipal.add(unpaidInterest);
            }
            if (currentPrincipal.compareTo(BigDecimal.ZERO) < 0) {
                currentPrincipal = BigDecimal.ZERO;
            }
        }
        BigDecimal finalInterestAccrued = processInterestAccrual(loan, lastCalculationDate, LocalDate.now(), currentPrincipal);
        return currentPrincipal.add(finalInterestAccrued);
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

    public void checkOverdueLoans() {
        List<Loan> activeLoans = loanDAO.getAllLoansByStatus(LoanStatus.ACTIVE);
        for (Loan loan : activeLoans) {
            if (isOverdue(loan)) {
                loan.setStatus(LoanStatus.DEFAULTED);
                loanDAO.update(loan);
                logger.warn("Status of loan was changed: DEFAULTED!");
            }
        }
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

    public List<Loan> getUpcomingExpirations() {
        return loanDAO.almostExpiredLoans().stream()
                .sorted(Comparator.comparing(Loan::getEndDate))
                .toList();
    }

    public List<Loan> getAllLoans() {
        return loanDAO.findAll();
    }


    public BigDecimal getTotalDebtByDebtor(User debtor) {
        return loanDAO.sumOfLoansByDebtor(debtor.getId(), LoanStatus.ACTIVE);
    }

    private boolean isOverdue(Loan loan) {
        return loan.getEndDate().isBefore(LocalDate.now());
    }
}
