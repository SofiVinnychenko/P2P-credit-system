package org.example.dao.impl;

import org.example.constants.LoanStatus;
import org.example.dao.AbstractQueriesDAO;
import org.example.dao.LoanDAO;
import org.example.model.Loan;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public class LoanDAOImpl extends AbstractQueriesDAO<Loan> implements LoanDAO {

    public LoanDAOImpl(SessionFactory sessionFactory) {
        super(Loan.class, sessionFactory);
    }

    public List<Loan> getLoansByCreditorId(Long creditorId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("select l from Loan l where l.creditor.id = :creditor", Loan.class)
                    .setParameter("creditor", creditorId).list();
        }
    }

    public List<Loan> getLoanByDebtorIdAndStatus(Long debtorId, LoanStatus loanStatus) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("select l from Loan l where l.debtor.id = :debtor and l.status = :status", Loan.class)
                    .setParameter("debtor", debtorId)
                    .setParameter("status", loanStatus)
                    .list();
        }
    }

    public BigDecimal sumOfLoansByDebtor(Long debtorId, LoanStatus loanStatus) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
             select coalesce(sum(l.amount), 0) from Loan l 
             where l.debtor.id = :debtor and l.status = :status""", BigDecimal.class)
                    .setParameter("debtor", debtorId)
                    .setParameter("status", loanStatus).uniqueResult();
        }
    }

    public List<Loan> almostExpiredLoans() {
        try (Session session = sessionFactory.openSession()) {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysFromNow = today.plusDays(7);

            return session.createQuery("""
            select l from Loan l 
            where l.status = :status and l.endDate 
            between :startDate and :endDate 
            order by l.endDate asc
            """, Loan.class)
                    .setParameter("status", LoanStatus.ACTIVE)
                    .setParameter("startDate", today)
                    .setParameter("endDate", sevenDaysFromNow)
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

}