package org.example.dao.impl;

import org.example.dao.AbstractQueriesDAO;
import org.example.dao.UserDAO;
import org.example.model.Loan;
import org.example.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class UserDAOImpl extends AbstractQueriesDAO<User> implements UserDAO {
    public UserDAOImpl(SessionFactory sessionFactory) {
        super(User.class, sessionFactory);
    }

    public List<User> getUsersByKeyword(String keyword) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
            select u from User u where u.firstName like :pattern or u.lastName like :pattern""", User.class)
                    .setParameter("pattern", "%" + keyword + "%")
                    .list();
        }
    }

    public User getGivenLoansByUser(Long id){
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
            select u from User u left join fetch u.givenLoans where u.id = :id""", User.class)
                    .setParameter("id", id).uniqueResult();
        }
    }

    public User getTakenLoansByUser(Long id){
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
            select u from User u left join fetch u.takenLoans where u.id = :id""", User.class)
                    .setParameter("id", id).uniqueResult();
        }
    }
}
