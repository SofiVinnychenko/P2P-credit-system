package org.example.config;

import lombok.Getter;
import lombok.Setter;
import org.example.dao.AbstractQueriesDAO;
import org.example.model.Loan;
import org.example.model.Payment;
import org.example.model.User;
import org.hibernate.SessionFactory;

@Getter
@Setter
public class AppContext {
    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();


}
