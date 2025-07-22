package org.example.dao;

import java.util.List;

public interface BaseMethodsDAO<T> {
    T save(T entity);
    T update(T entity);
    T findById(Long id);
    List<T> findAll();
}
