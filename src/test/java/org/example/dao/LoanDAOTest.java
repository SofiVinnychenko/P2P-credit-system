package org.example.dao;

import lombok.extern.slf4j.Slf4j;
import org.example.config.HibernateUtil;
import org.example.constants.LoanStatus;
import org.example.dao.impl.LoanDAOImpl;
import org.example.dao.impl.UserDAOImpl;
import org.example.model.Loan;
import org.example.model.User;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LoanDAOTest {

    private static SessionFactory sessionFactory;
    private static UserDAO userDAO;
    private static LoanDAO loanDAO;
    private static List<Object> objectList;

    @BeforeAll
    public static void setUp() {
        sessionFactory = HibernateUtil.getSessionFactory();
        userDAO = new UserDAOImpl(sessionFactory);
        loanDAO = new LoanDAOImpl(sessionFactory);

        objectList = new ArrayList<>();
    }

    public static List<Object> createData() {
        if (objectList.isEmpty()) {
            User creditor = User.builder()
                    .firstName("creditorName")
                    .lastName("creditorLastName")
                    .phoneNumber("0997655456")
                    .email("creditor@gmail.com")
                    .build();
            userDAO.save(creditor);
            objectList.add(creditor);

            User creditor1 = User.builder()
                    .firstName("creditorName1")
                    .lastName("creditorLastName1")
                    .phoneNumber("0997655458")
                    .email("creditor1@gmail.com")
                    .build();
            userDAO.save(creditor1);
            objectList.add(creditor1);

            User debtor = User.builder()
                    .firstName("debtorName")
                    .lastName("debtorLastName")
                    .phoneNumber("0987655459")
                    .email("debtor@gmail.com")
                    .build();
            userDAO.save(debtor);
            objectList.add(debtor);

            Loan loan = Loan.builder()
                    .amount(new BigDecimal("50000"))
                    .creditor(creditor)
                    .debtor(debtor)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(2))
                    .interestRate(new BigDecimal("0.5"))
                    .status(LoanStatus.ACTIVE)
                    .build();
            loanDAO.save(loan);
            objectList.add(loan);

            Loan loan1 = Loan.builder()
                    .amount(new BigDecimal("50000"))
                    .creditor(creditor1)
                    .debtor(debtor)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(5))
                    .interestRate(new BigDecimal("0.8"))
                    .status(LoanStatus.ACTIVE)
                    .build();
            loanDAO.save(loan1);
            objectList.add(loan1);

            Loan loan2 = Loan.builder()
                    .amount(new BigDecimal("70000"))
                    .creditor(creditor1)
                    .debtor(debtor)
                    .startDate(LocalDate.now().minusMonths(7))
                    .endDate(LocalDate.now().plusMonths(1))
                    .interestRate(new BigDecimal("0.6"))
                    .status(LoanStatus.REPAID)
                    .build();
            loanDAO.save(loan2);
            objectList.add(loan2);

            Loan loan3 = Loan.builder()
                    .amount(new BigDecimal("70000"))
                    .creditor(creditor1)
                    .debtor(debtor)
                    .startDate(LocalDate.now().minusMonths(1))
                    .endDate(LocalDate.now())
                    .interestRate(new BigDecimal("0.5"))
                    .status(LoanStatus.DEFAULTED)
                    .build();
            loanDAO.save(loan3);
            objectList.add(loan3);
        }
        return objectList;
    }

    @Test
    public void getLoansByCreditorIdTest() {

        User creditor = (User) createData().get(0);
        List<Loan> loansByCreditorId = loanDAO.getLoansByCreditorId(creditor.getId());
        assertNotNull(loansByCreditorId);
        assertEquals(1, loansByCreditorId.size());
    }

    @Test
    public void getLoanByDebtorIdAndStatusTest() {

        User debtor = (User) createData().get(2);
        List<Loan> loansByDebtor = loanDAO.getLoanByDebtorIdAndStatus(debtor.getId(), LoanStatus.ACTIVE);
        assertNotNull(loansByDebtor);
        assertEquals(2, loansByDebtor.size());
    }

    @Test
    public void sumOfLoansByDebtorTest() {
        User debtor = (User) createData().get(2);
        BigDecimal sum = loanDAO.sumOfLoansByDebtor(debtor.getId(), LoanStatus.ACTIVE);
        assertNotNull(sum);
        assertEquals(0, sum.compareTo(BigDecimal.valueOf(100000.00)));
    }

    @Test
    public void almostExpiredLoansTest() {
        List<Loan> almostExpiredLoans = loanDAO.almostExpiredLoans();
        assertNotNull(almostExpiredLoans);
        assertEquals(1, almostExpiredLoans.size());
    }

    @Test
    public void getAllLoansByStatusTest() {
        List<Loan> loansByStatus = loanDAO.getAllLoansByStatus(LoanStatus.ACTIVE);
        assertNotNull(loansByStatus);
        assertEquals(2, loansByStatus.size());
    }

    @AfterAll
    public static void cleanup() {
        var session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        session.createQuery("DELETE FROM Loan").executeUpdate();
        session.createQuery("DELETE FROM User").executeUpdate();
        tx.commit();
        session.close();
    }


}
