package org.example.dao;

import java.util.List;

public interface BaseMethodsDAO<T> {
    T saveOrUpdate(T entity);
    T findById(int id);
    void delete(T entity);
    List<T> findAll();
}
