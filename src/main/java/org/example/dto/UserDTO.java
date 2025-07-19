package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserDTO {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private BigDecimal balance;

    public boolean isValidUserData() {
        return (firstName != null || !firstName.trim().isEmpty()) && (lastName != null || !lastName.trim().isEmpty())
                && (phoneNumber != null || !phoneNumber.trim().isEmpty()) && (email != null || !email.trim().isEmpty())
                && balance.compareTo(BigDecimal.ZERO) > 0;
    }
}
