package org.example.config;

import lombok.Getter;
import lombok.Setter;
import org.example.dao.AbstractQueriesDAO;
import org.example.dao.LoanDAO;
import org.example.dao.PaymentDAO;
import org.example.dao.UserDAO;
import org.example.dao.impl.LoanDAOImpl;
import org.example.dao.impl.PaymentDAOImpl;
import org.example.dao.impl.UserDAOImpl;
import org.example.model.Loan;
import org.example.model.Payment;
import org.example.model.User;
import org.hibernate.SessionFactory;

@Getter
@Setter
public class AppContext {
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    private final UserDAO userDAO = new UserDAOImpl(sessionFactory);
    private final PaymentDAO paymentDAO = new PaymentDAOImpl(sessionFactory);
    private final LoanDAO loanDAO = new LoanDAOImpl(sessionFactory);

}
