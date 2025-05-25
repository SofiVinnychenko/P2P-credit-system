package org.example.dao.impl;

import org.example.dao.AbstractQueriesDAO;
import org.example.dao.UserDAO;
import org.example.model.User;
import org.hibernate.SessionFactory;

public class UserDAOImpl extends AbstractQueriesDAO<User> implements UserDAO {
    public UserDAOImpl(SessionFactory sessionFactory) {
        super(User.class, sessionFactory);
    }
}
