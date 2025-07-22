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
                .build();
        userDAO.save(user);
        return user;
    }

    public List<User> getUsersByKeyword(String keyword) {
        return userDAO.getUsersByKeyword(keyword);
    }

    public User updateUser(Long id, UserDTO userDTO) {
        User userById = userDAO.findById(id);
        if (userById == null) {
            throw new IllegalStateException("User with id " + id + " not found");
        }
        if (userDTO.getFirstName() != null && !userDTO.getFirstName().trim().isEmpty()) {
            userById.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null && !userDTO.getLastName().trim().isEmpty()) {
            userById.setLastName(userDTO.getLastName());
        }
        if (userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().trim().isEmpty()) {
            userById.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            userById.setEmail(userDTO.getEmail());
        }
        return userDAO.update(userById);
    }

    public User getTakenLoansByUser(User user) {
        return userDAO.getTakenLoansByUser(user.getId());
    }

    public User getGivenLoansByUser(User user) {
        return userDAO.getGivenLoansByUser(user.getId());
    }
}
