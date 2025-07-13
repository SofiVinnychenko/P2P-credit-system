package org.example.dao.impl;

import org.example.constants.LoanStatus;
import org.example.dao.AbstractQueriesDAO;
import org.example.dao.LoanDAO;
import org.example.model.Loan;
import org.example.model.Payment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LoanDAOImpl extends AbstractQueriesDAO<Loan> implements LoanDAO {

    public LoanDAOImpl(SessionFactory sessionFactory) {
        super(Loan.class, sessionFactory);
    }

    public List<Loan> getLoansByCreditorId(int creditorId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("select l from Loan l where l.creditor.id = :creditor", Loan.class)
                    .setParameter("creditor", creditorId).list();
        }
    }

    public List<Loan> getLoanByDebtorIdAndStatus(int debtorId, LoanStatus loanStatus) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("select l from Loan l where l.debtor.id = :debtor and l.status = :status", Loan.class)
                    .setParameter("debtor", debtorId)
                    .setParameter("status", loanStatus)
                    .list();
        }
    }

    public BigDecimal sumOfLoansByDebtor(int debtorId, LoanStatus loanStatus) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
             select coalesce(sum(l.amount), 0) from Loan l where l.debtor.id = :debtor and l.status = :status""", BigDecimal.class)
                    .setParameter("debtor", debtorId)
                    .setParameter("status", loanStatus).uniqueResult();
        }
    }

    public BigDecimal avgOfInterestRateByLastMonth() {
        try (Session session = sessionFactory.openSession()) {
            LocalDate oneMonth = LocalDate.now().minusMonths(1);
            return session.createQuery(
                            """
            select coalesce(avg(l.interestRate), 0) from Loan l where l.startDate >= :oneMonth""", BigDecimal.class)
                    .setParameter("oneMonth", oneMonth)
                    .uniqueResult();
        }
    }

    public List<Loan> almostExpiredLoans() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
             select l from Loan l where l.endDate >= CURRENT_DATE - 7""", Loan.class)
                    .list();
        }
    }

    public List<Loan> getAllLoansByStatus(LoanStatus loanStatus) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
            select l from Loan l where l.status = :status""", Loan.class)
                    .setParameter("status", loanStatus)
                    .list();
        }
    }

    public Loan getPaymentsByLoan(int loanId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
            select l from Loan l left join fetch l.payments where l.id = :loanId""", Loan.class)
                    .setParameter("loanId", loanId).uniqueResult();
        }
    }

}