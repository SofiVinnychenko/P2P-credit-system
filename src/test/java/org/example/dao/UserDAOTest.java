package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.constants.LoanStatus;
import org.example.dao.impl.LoanDAOImpl;
import org.example.dao.impl.UserDAOImpl;
import org.example.model.Loan;
import org.example.model.User;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserDAOTest {

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

    public static List<Object> data() {
        if (objectList.isEmpty()) {
            User creditor = User.builder()
                    .firstName("TestName")
                    .lastName("TestLastName")
                    .phoneNumber("0987655456")
                    .email("popa@gmail.com")
                    .build();
            userDAO.save(creditor);
            objectList.add(creditor);

            User debtor = User.builder()
                    .firstName("debtorName")
                    .lastName("debtorSurName")
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
                    .endDate(LocalDate.now().plusMonths(4))
                    .interestRate(new BigDecimal("0.5"))
                    .status(LoanStatus.ACTIVE)
                    .build();
            loanDAO.save(loan);
            objectList.add(loan);
        }
        return objectList;
    }

    @Test
    public void saveAndUpdateValidUserTest() {

        User user = (User) data().get(0);
        assertNotNull(user.getId());
        User receivedUser = userDAO.findById(user.getId());
        assertEquals("TestName", receivedUser.getFirstName());
        assertEquals("0987655456", receivedUser.getPhoneNumber());

        receivedUser.setFirstName("TestUpdate");
        userDAO.update(receivedUser);
        assertEquals("TestUpdate", receivedUser.getFirstName());
    }

    @Test
    public void getUsersByKeywordTest() {

        data();
        String keyword = "Last";
        List<User> users = userDAO.getUsersByKeyword(keyword);
        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    public void testGetGivenLoansByUser() {

        List<Object> data = data();
        User creditor = (User) data.get(0);
        User debtor = (User) data.get(1);

        User givenLoansByUser = userDAO.getGivenLoansByUser(creditor.getId());
        assertNotNull(givenLoansByUser);
        List<Loan> givenLoans = givenLoansByUser.getGivenLoans();
        assertNotNull(givenLoans);
        assertEquals(1, givenLoans.size());

        Loan given = givenLoans.get(0);
        assertEquals(debtor.getId(), given.getDebtor().getId());

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
