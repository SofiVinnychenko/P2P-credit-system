package org.example.dao;

import org.example.model.Payment;
import org.example.model.User;

import java.util.List;

public interface UserDAO extends BaseMethodsDAO<User> {

    User getGivenLoansByUser(Long id);

    User getTakenLoansByUser(Long id);

    List<User> getUsersByKeyword(String keyword);
}
