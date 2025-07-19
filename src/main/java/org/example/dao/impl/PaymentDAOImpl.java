package org.example.dao.impl;

import org.example.constants.LoanStatus;
import org.example.constants.PaymentType;
import org.example.dao.AbstractQueriesDAO;
import org.example.dao.PaymentDAO;
import org.example.model.Loan;
import org.example.model.Payment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.util.List;

public class PaymentDAOImpl extends AbstractQueriesDAO<Payment> implements PaymentDAO {
    public PaymentDAOImpl(SessionFactory sessionFactory) {
        super(Payment.class, sessionFactory);
    }

    public List<Payment> getPaymentsByLoanIdAndType(Long loanId, PaymentType paymentType) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("select p from Payment p where p.loan.id = :loan and p.type = :type", Payment.class)
                    .setParameter("loan", loanId)
                    .setParameter("type", paymentType)
                    .list();
        }
    }

    public List<Payment> getAllPaymentsByType(PaymentType paymentType) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
            select p from Payment p where p.type = :type""", Payment.class)
                    .setParameter("type", paymentType)
                    .list();
        }
    }
}
