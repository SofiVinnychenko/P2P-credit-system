package org.example.service;

import org.example.config.HibernateUtil;
import org.example.constants.LoanStatus;
import org.example.dao.LoanDAO;
import org.example.dao.PaymentDAO;
import org.example.dao.UserDAO;
import org.example.dao.impl.LoanDAOImpl;
import org.example.dao.impl.PaymentDAOImpl;
import org.example.dao.impl.UserDAOImpl;
import org.example.dto.LoanDTO;
import org.example.dto.UserDTO;
import org.example.model.Loan;
import org.example.model.Payment;
import org.example.model.User;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentServiceTest {

    private static SessionFactory sessionFactory;
    private static UserService userService;
    private LoanService loanService;
    private PaymentService paymentService;
    private static LoanDAO loanDAO;

    private TestData testData;

    @BeforeAll
    public static void setUpClass() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }

    @BeforeEach
    public void setUp() {
        loanDAO = new LoanDAOImpl(sessionFactory);
        PaymentDAO paymentDAO = new PaymentDAOImpl(sessionFactory);
        UserDAO userDAO = new UserDAOImpl(sessionFactory);
        loanService = new LoanService(loanDAO);
        paymentService = new PaymentService(paymentDAO, loanService);
        userService = new UserService(userDAO);

        cleanup();
        testData = createData();
    }

    public static TestData createData() {
        TestData td = new TestData();

        td.creditor = createUser("Kelly", "Doe", "122", "kelly@gmail.com");
        td.debtor = createUser("Linda", "Spirit", "345", "sdfg@gmail.com");

        return td;
    }

    private static User createUser(String first, String last, String phone, String email) {
        UserDTO dto = new UserDTO();
        dto.setFirstName(first);
        dto.setLastName(last);
        dto.setPhoneNumber(phone);
        dto.setEmail(email);
        return userService.createUser(dto);
    }

    private static Loan createLoan(BigDecimal amount, User creditor, User debtor, LocalDate start, LocalDate end, BigDecimal rate, LoanStatus status) {
        Loan loan = Loan.builder()
                .amount(amount)
                .creditor(creditor)
                .debtor(debtor)
                .startDate(start)
                .endDate(end)
                .interestRate(rate)
                .status(status)
                .build();
        loanDAO.save(loan);
        return loan;
    }

    @Test
    public void paidPaymentTest() {
        Loan loan = createLoan(BigDecimal.valueOf(8000), testData.creditor, testData.debtor, LocalDate.now().minusDays(92), LocalDate.now().plusDays(30), new BigDecimal("0.15"), LoanStatus.ACTIVE);

        Payment firstPayment = loan.getPayments().get(0);
        paymentService.paidPayment(firstPayment);


    }

    @AfterAll
    public static void cleanup() {
        try (var session = sessionFactory.openSession()) {
            var tx = session.beginTransaction();
            try {
                session.createQuery("DELETE FROM Payment").executeUpdate();
                session.createQuery("DELETE FROM Loan").executeUpdate();
                session.createQuery("DELETE FROM User").executeUpdate();
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    static class TestData {
        User creditor;
        User debtor;
    }
}
