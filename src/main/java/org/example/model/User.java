package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "phone_number")
    private String phoneNumber;
    private String email;
    private BigDecimal balance;
    @OneToMany(mappedBy = "creditor", fetch = FetchType.LAZY)
    private List<Loan> givenLoans;
    @OneToMany(mappedBy = "debtor", fetch = FetchType.LAZY)
    private List<Loan> takenLoans;
}
