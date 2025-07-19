package org.example.dao;

import org.example.constants.PaymentType;
import org.example.model.Loan;
import org.example.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentDAO extends BaseMethodsDAO<Payment>{

    List<Payment> getPaymentsByLoanIdAndType(Long loanId, PaymentType paymentType);

    List<Payment> getAllPaymentsByType(PaymentType paymentType);

}
