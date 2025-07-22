package org.example.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public abstract class AbstractQueriesDAO<T> implements BaseMethodsDAO<T>{
    private final Class<T> aClass;
    protected final SessionFactory sessionFactory;

    public AbstractQueriesDAO(Class<T> aClass, SessionFactory sessionFactory) {
        this.aClass = aClass;
        this.sessionFactory = sessionFactory;
    }

    public T save(T entity) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            return entity;
        }
    }

    public T update(T entity) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            T mergedEntity = (T) session.merge(entity);
            transaction.commit();
            return mergedEntity;
        }
    }

    public T findById(Long id) {
        try (Session session = sessionFactory.openSession()){
            return session.get(aClass, id);
        }
    }

    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from " + aClass.getName(), aClass).list();
        }
    }
}
