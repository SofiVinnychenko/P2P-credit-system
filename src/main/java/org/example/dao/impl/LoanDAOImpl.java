package org.example.dao.impl;

import org.example.dao.AbstractQueriesDAO;
import org.example.dao.LoanDAO;
import org.example.model.Loan;
import org.hibernate.SessionFactory;

public class LoanDAOImpl extends AbstractQueriesDAO<Loan> implements LoanDAO{
    public LoanDAOImpl(SessionFactory sessionFactory) {
        super(Loan.class, sessionFactory);
    }
}
