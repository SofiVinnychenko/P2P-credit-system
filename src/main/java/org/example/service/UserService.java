package org.example.service;

import org.example.constants.LoanStatus;
import org.example.dao.UserDAO;
import org.example.dto.UserDTO;
import org.example.model.Loan;
import org.example.model.Payment;
import org.example.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User createUser(UserDTO userDTO) {
        if (!userDTO.isValidUserData()) {
            throw new IllegalArgumentException("Invalid user creation data");
        }
        User user = User.builder()
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .phoneNumber(userDTO.getPhoneNumber())
                .email(userDTO.getEmail())
                .balance(userDTO.getBalance())
                .build();
        userDAO.saveOrUpdate(user);
        return user;
    }

    public List<User> getUsersByKeyword(String keyword) {
        return userDAO.getUsersByKeyword(keyword);
    }

    public void updateUser(Long id, UserDTO userDTO) {
        User userById = userDAO.findById(id);
        if (userById == null) {
            throw new IllegalStateException("User with id " + id + " not found");
        }

        if (userDTO.getFirstName() != null && !userDTO.getFirstName().isEmpty()) {
            userById.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null && !userDTO.getLastName().isEmpty()) {
            userById.setLastName(userDTO.getLastName());
        }
        if (userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().isEmpty()) {
            userById.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            userById.setEmail(userDTO.getEmail());
        }
        userDAO.saveOrUpdate(userById);
    }

    public User getTakenLoansByUser(Long id) {
        return userDAO.getTakenLoansByUser(id);
    }


}
