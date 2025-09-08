package org.example.service;

import org.example.constants.LoanStatus;
import org.example.constants.PaymentType;
import org.example.dao.PaymentDAO;
import org.example.model.Loan;
import org.example.model.Payment;
import org.example.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PaymentService {

    private final PaymentDAO paymentDAO;
    private final LoanService loanService;

    public PaymentService(PaymentDAO paymentDAO, LoanService loanService) {
        this.paymentDAO = paymentDAO;
        this.loanService = loanService;
    }

    public List<Payment> generateAndSaveSchedule(Loan loan) {
        List<Payment> schedule = generateDifferentiatedSchedule(loan);
        for (Payment payment : schedule) {
            paymentDAO.save(payment);
        }
        return schedule;
    }

    public List<Payment> generateDifferentiatedSchedule(Loan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("Loan object is null");
        }
        List<Payment> schedule = new ArrayList<>();
        BigDecimal remainingPrincipal = loan.getAmount();
        int months = (int) ChronoUnit.MONTHS.between(loan.getStartDate(), loan.getEndDate());
        BigDecimal monthlyPrincipal = loan.getAmount()
                .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        LocalDate paymentDate = loan.getStartDate().plusMonths(1);

        LocalDate lastDate = loan.getStartDate();

        for (int i = 0; i < months; i++) {

            BigDecimal interest = loanService.processInterestAccrual(loan, lastDate, paymentDate, remainingPrincipal);
            BigDecimal totalPayment = monthlyPrincipal.add(interest).setScale(2, RoundingMode.HALF_UP);

            Payment payment = Payment.builder()
                    .loan(loan)
                    .amount(totalPayment)
                    .dueDate(paymentDate)
                    .paidDate(null)
                    .type(PaymentType.PENDING)
                    .build();
            schedule.add(payment);

            remainingPrincipal = remainingPrincipal.subtract(monthlyPrincipal);
            lastDate = paymentDate;
            paymentDate = paymentDate.plusMonths(1);
        }
        return schedule;
    }

    public void paidPayment(Payment payment) {
        if (payment.getType() == PaymentType.PAID) {
            throw new IllegalStateException("Цей платіж вже був сплачений");
        }
        payment.setPaidDate(LocalDate.now());
        payment.setType(PaymentType.PAID);
        paymentDAO.update(payment);

        if (LocalDate.now().isAfter(payment.getDueDate())) {
            recalculateRemainingSchedule(payment.getLoan());
        }
    }

    public void recalculateRemainingSchedule(Loan loan) {
        List<Payment> unpaidPayments = loan.getPayments().stream()
                .filter(payment -> payment.getType() == PaymentType.PENDING)
                .sorted(Comparator.comparing(Payment::getDueDate)).toList();
        if (unpaidPayments.isEmpty()) {
            return;
        }
        int totalMonths = (int) ChronoUnit.MONTHS.between(loan.getStartDate(), loan.getEndDate());
        BigDecimal monthlyPrincipal = loan.getAmount()
                .divide(BigDecimal.valueOf(totalMonths), 2, RoundingMode.HALF_UP);

        LocalDate lastPaidDate = getLastPaidPaymentDate(loan);
        BigDecimal remainingPrincipal = calculateRemainingPrincipal(loan, monthlyPrincipal);

        recalculatePayments(unpaidPayments, remainingPrincipal, lastPaidDate, loan, monthlyPrincipal);
    }

    private LocalDate getLastPaidPaymentDate(Loan loan) {
        return loan.getPayments().stream()
                .filter(payment -> payment.getType() == PaymentType.PAID)
                .max(Comparator.comparing(Payment::getPaidDate))
                .map(Payment::getPaidDate)
                .orElse(loan.getStartDate());
    }

    private BigDecimal calculateRemainingPrincipal(Loan loan, BigDecimal monthlyPrincipal) {
        int paidMonths = (int) loan.getPayments().stream()
                .filter(payment -> payment.getType() == PaymentType.PAID)
                .count();

        return loan.getAmount().subtract(
                monthlyPrincipal.multiply(BigDecimal.valueOf(paidMonths))
        );
    }

    private void recalculatePayments(List<Payment> unpaidPayments, BigDecimal remainingPrincipal,
                                     LocalDate lastPaidDate, Loan loan, BigDecimal monthlyPrincipal) {

        BigDecimal currentPrincipal = remainingPrincipal;
        LocalDate previousDate = lastPaidDate;

        for (Payment payment : unpaidPayments) {
            BigDecimal interest = loanService.processInterestAccrual(loan, previousDate, payment.getDueDate(), currentPrincipal);
            BigDecimal newAmount = monthlyPrincipal.add(interest).setScale(2, RoundingMode.HALF_UP);

            payment.setAmount(newAmount);
            paymentDAO.update(payment);

            currentPrincipal = currentPrincipal.subtract(monthlyPrincipal);
            if (currentPrincipal.compareTo(BigDecimal.ZERO) < 0) {
                currentPrincipal = BigDecimal.ZERO;
            }

            previousDate = payment.getDueDate();
        }
    }

    public List<Payment> getPaymentsByLoanIdAndType(Loan loan, PaymentType paymentType) {
        return paymentDAO.getPaymentsByLoanIdAndType(loan.getId(), paymentType);
    }

    public List<Payment> getAllPaymentsByType(PaymentType paymentType) {
        return paymentDAO.getAllPaymentsByType(paymentType);
    }
}
