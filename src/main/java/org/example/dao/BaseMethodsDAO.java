package org.example.dao;

import java.util.List;

public interface BaseMethodsDAO<T> {
    T saveOrUpdate(T entity);
    T findById(Long id);
    List<T> findAll();
}
