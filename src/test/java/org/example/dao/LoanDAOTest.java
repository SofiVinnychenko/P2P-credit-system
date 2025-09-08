package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.constants.LoanStatus;
import org.example.dao.impl.LoanDAOImpl;
import org.example.dao.impl.UserDAOImpl;
import org.example.model.Loan;
import org.example.model.User;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LoanDAOTest {

    private static SessionFactory sessionFactory;
    private static UserDAO userDAO;
    private static LoanDAO loanDAO;

    private TestData testData;

    @BeforeAll
    public static void setUp() {
        sessionFactory = HibernateUtil.getSessionFactory();
        userDAO = new UserDAOImpl(sessionFactory);
        loanDAO = new LoanDAOImpl(sessionFactory);
    }

    @BeforeEach
    public void initData() {
        cleanup();
        testData = createData();
    }

    public static TestData createData() {
        TestData td = new TestData();

        td.creditor = createUser("creditorName", "creditorLastName", "0997655456", "creditor@gmail.com");
        td.creditor1 = createUser("creditorName1", "creditorLastName1", "0997655458", "creditor1@gmail.com");
        td.debtor = createUser("debtorName", "debtorLastName", "0987655459", "debtor@gmail.com");

        td.loan = createLoan(new BigDecimal("50000"), td.creditor, td.debtor, LocalDate.now(), LocalDate.now().plusDays(2), new BigDecimal("0.5"), LoanStatus.ACTIVE);
        td.loan1 = createLoan(new BigDecimal("50000"), td.creditor1, td.debtor, LocalDate.now(), LocalDate.now().plusMonths(5), new BigDecimal("0.8"), LoanStatus.ACTIVE);
        td.loan2 = createLoan(new BigDecimal("70000"), td.creditor1, td.debtor, LocalDate.now().minusMonths(7), LocalDate.now().plusMonths(1), new BigDecimal("0.6"), LoanStatus.REPAID);
        td.loan3 = createLoan(new BigDecimal("70000"), td.creditor1, td.debtor, LocalDate.now().minusMonths(1), LocalDate.now(), new BigDecimal("0.5"), LoanStatus.DEFAULTED);

        return td;
    }

    private static User createUser(String firstName, String lastName, String phone, String email) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phone)
                .email(email)
                .build();
        userDAO.save(user);
        return user;
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
    public void getLoansByCreditorIdTest() {

        List<Loan> loansByCreditorId = loanDAO.getLoansByCreditorId(testData.creditor.getId());
        assertNotNull(loansByCreditorId);
        assertEquals(1, loansByCreditorId.size());
    }

    @Test
    public void getLoanByDebtorIdAndStatusTest() {

        List<Loan> loansByDebtor = loanDAO.getLoanByDebtorIdAndStatus(testData.debtor.getId(), LoanStatus.ACTIVE);
        assertNotNull(loansByDebtor);
        assertEquals(2, loansByDebtor.size());
    }

    @Test
    public void sumOfLoansByDebtorTest() {
        BigDecimal sum = loanDAO.sumOfLoansByDebtor(testData.debtor.getId(), LoanStatus.ACTIVE);
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
        try (var session = sessionFactory.openSession()) {
            var tx = session.beginTransaction();
            try {
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
        User creditor1;
        User debtor;
        Loan loan;
        Loan loan1;
        Loan loan2;
        Loan loan3;
    }


}
