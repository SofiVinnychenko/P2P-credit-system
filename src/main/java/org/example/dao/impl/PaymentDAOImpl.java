package org.example.dao.impl;

import org.example.dao.AbstractQueriesDAO;
import org.example.dao.PaymentDAO;
import org.example.model.Payment;
import org.hibernate.SessionFactory;

public class PaymentDAOImpl extends AbstractQueriesDAO<Payment> implements PaymentDAO {
    public PaymentDAOImpl(SessionFactory sessionFactory) {
        super(Payment.class, sessionFactory);
    }
}
