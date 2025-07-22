package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;

    public boolean isValidUserData() {
        return (firstName != null && !firstName.trim().isEmpty())
                && (lastName != null && !lastName.trim().isEmpty())
                && (phoneNumber != null && !phoneNumber.trim().isEmpty())
                && (email != null && !email.trim().isEmpty());
    }
}
