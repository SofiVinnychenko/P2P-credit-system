package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.HibernateUtil;
import org.example.constants.LoanStatus;
import org.example.constants.PaymentType;
import org.example.dao.LoanDAO;
import org.example.dao.LoanDAOTest;
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
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
public class LoanServiceTest {

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
        LoanServiceTest.TestData td = new TestData();

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

    private LoanDTO createLoanDTO(User creditor, User debtor,
                                  BigDecimal amount, int term, BigDecimal rate) {
        LoanDTO dto = new LoanDTO();
        dto.setAmount(amount);
        dto.setCreditor(creditor);
        dto.setDebtor(debtor);
        dto.setTerm(term);
        dto.setInterestRate(rate);
        return dto;
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

    @ParameterizedTest
    @ValueSource(ints = {5, 8, 12})
    public void createValidLoanAndPaymentsTest(int term) {

        LoanDTO loanDTO = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("50000"), term, new BigDecimal("0.01"));
        Loan loan = loanService.createLoan(loanDTO, paymentService);

        assertNotNull(loan);
        assertEquals(0, loan.getStartDate().plusMonths(term).compareTo(loan.getEndDate()));
        assertNotNull(loan.getPayments());
        assertEquals(term, loan.getPayments().size());

        Payment firstPayment = loan.getPayments().get(0);
        assertEquals(0, loan.getStartDate().plusMonths(1).compareTo(firstPayment.getDueDate()));
        assertNull(firstPayment.getPaidDate());
        assertEquals(PaymentType.PENDING, firstPayment.getType());

        Payment secondPayment = loan.getPayments().get(1);
        assertEquals(0, loan.getStartDate().plusMonths(2).compareTo(secondPayment.getDueDate()));
        assertNull(firstPayment.getPaidDate());
        assertEquals(PaymentType.PENDING, firstPayment.getType());
    }

    @ParameterizedTest
    @CsvSource({"0, 8, 0.01", "20000, 13, 0.01", "20000, 8, 0"})
    public void createInvalidLoanAndPaymentsTest(BigDecimal amount, int term, BigDecimal rate) {

        LoanDTO loanDTO = createLoanDTO(testData.creditor, testData.debtor, amount, term, rate);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> loanService.createLoan(loanDTO, paymentService));
        assertEquals("Invalid loan creation data", exception.getMessage());
    }

    @Test
    public void canDebtorTakeNewLoanValidTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("10000"), 8, new BigDecimal("0.01"));
        loanService.createLoan(loanDTO1, paymentService);

        LoanDTO loanDTO2 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("20000"), 8, new BigDecimal("0.01"));
        loanService.createLoan(loanDTO2, paymentService);

        LoanDTO loanDTO3 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("10000"), 8, new BigDecimal("0.01"));
        assertTrue(loanService.canDebtorTakeNewLoan(loanDTO3.getDebtor()));
    }

    @Test
    public void canDebtorTakeNewLoanInvalidTest1() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("50000"), 8, new BigDecimal("0.01"));
        loanService.createLoan(loanDTO1, paymentService);

        LoanDTO loanDTO2 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("20000"), 8, new BigDecimal("0.01"));

        Exception exception = assertThrows(IllegalStateException.class, () -> loanService.createLoan(loanDTO2, paymentService));
        assertEquals("Debtor cannot take new loan", exception.getMessage());
    }

    @Test
    public void canDebtorTakeNewLoanInvalidTest2() {

        createLoan(new BigDecimal("50000"), testData.creditor, testData.debtor, LocalDate.now(), LocalDate.now().plusMonths(4), new BigDecimal("0.5"), LoanStatus.DEFAULTED);

        LoanDTO loanDTO = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("50000"), 8, new BigDecimal("0.01"));

        Exception exception = assertThrows(IllegalStateException.class, () -> loanService.createLoan(loanDTO, paymentService));
        assertEquals("Debtor cannot take new loan", exception.getMessage());
    }

    @Test
    public void updateLoanStatusTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("50000"), 8, new BigDecimal("0.01"));
        Loan loan = loanService.createLoan(loanDTO1, paymentService);

        loanService.updateLoanStatus(loan, LoanStatus.DEFAULTED);
        assertEquals(LoanStatus.DEFAULTED, loan.getStatus());

        loanService.updateLoanStatus(loan, LoanStatus.REPAID);
        assertEquals(0, LocalDate.now().compareTo(loan.getEndDate()));
    }

    @Test
    public void closeLoanValidTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("50000"), 8, new BigDecimal("0.01"));
        Loan loan = loanService.createLoan(loanDTO1, paymentService);

        List<Payment> payments = loan.getPayments();
        for (Payment payment : payments) {
            paymentService.paidPayment(payment);
        }
        loanService.closeLoan(loan);
        assertEquals(LoanStatus.REPAID, loan.getStatus());
    }

    @Test
    public void closeLoanInvalidTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("50000"), 8, new BigDecimal("0.01"));
        Loan loan = loanService.createLoan(loanDTO1, paymentService);

        assertThrows(IllegalStateException.class, () -> loanService.closeLoan(loan));
    }

    @Test
    public void getActiveLoansForCreditorInThisYearTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("50000"), 8, new BigDecimal("0.01"));
        loanService.createLoan(loanDTO1, paymentService);

        List<Loan> activeLoans = loanService.getActiveLoansForCreditorInThisYear(testData.creditor);
        assertEquals(1, activeLoans.size());
    }

    @ParameterizedTest
    @NullSource
    public void shouldExceptionWhenLoanNull(Loan nullSource) {
        assertThrows(IllegalArgumentException.class, () -> loanService.updateLoanStatus(nullSource, LoanStatus.REPAID));
        assertThrows(IllegalArgumentException.class, () -> loanService.closeLoan(nullSource));
        assertThrows(IllegalArgumentException.class, () -> loanService.calculateRemainingDebt(nullSource));
    }

    @ParameterizedTest
    @NullSource
    public void shouldExceptionWhenUserNull(User nullSource) {
        assertThrows(IllegalArgumentException.class, () -> loanService.getActiveLoansForCreditorInThisYear(nullSource));
        assertThrows(IllegalArgumentException.class, () -> loanService.getActiveLoansForDebtor(nullSource));
        assertThrows(IllegalArgumentException.class, () -> loanService.getActiveLoansForCreditorInThisYear(nullSource));
        assertThrows(IllegalArgumentException.class, () -> loanService.canDebtorTakeNewLoan(nullSource));
    }

    @Test
    public void calculateRemainingDebtTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("20000"), 4, new BigDecimal("0.15"));
        Loan loan = loanService.createLoan(loanDTO1, paymentService);

        Payment payment = loan.getPayments().get(0);
        paymentService.paidPayment(payment);
        BigDecimal debt = loanService.calculateRemainingDebt(loan);

        assertEquals(0, BigDecimal.valueOf(15373.97).compareTo(debt), 1);
    }

    @Test
    public void processInterestAccrualTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("20000"), 4, new BigDecimal("0.15"));
        Loan loan = loanService.createLoan(loanDTO1, paymentService);

        BigDecimal interest = loanService.processInterestAccrual(loan, loan.getStartDate(), loan.getStartDate().plusMonths(1), loan.getAmount());
        assertEquals(0, BigDecimal.valueOf(254.79).compareTo(interest), 1);
    }

    @Test
    public void processInterestAccrualInvalidFirstTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("20000"), 4, new BigDecimal("0.15"));
        Loan loan = loanService.createLoan(loanDTO1, paymentService);

        BigDecimal first = loanService.processInterestAccrual(loan, loan.getStartDate(), loan.getStartDate().plusMonths(0), loan.getAmount());
        assertEquals(BigDecimal.ZERO, first);

        BigDecimal second = loanService.processInterestAccrual(loan, loan.getStartDate(), loan.getStartDate().minusMonths(1), loan.getAmount());
        assertEquals(BigDecimal.ZERO, second);
    }

    @Test
    public void processInterestAccrualInvalidSecondTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("20000"), 4, new BigDecimal("0.15"));
        Loan loan = loanService.createLoan(loanDTO1, paymentService);

        BigDecimal nullable = loanService.processInterestAccrual(loan, loan.getStartDate(), loan.getStartDate().plusMonths(0), BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, nullable);
    }

    @Test
    public void checkOverdueLoansTest() {

        LoanDTO loanDTO1 = createLoanDTO(testData.creditor, testData.debtor, new BigDecimal("20000"), 4, new BigDecimal("0.15"));
        loanService.createLoan(loanDTO1, paymentService);

        assertFalse(loanService.checkOverdueLoans());
    }

    @Test
    public void checkOverdueLoansInvalidTest() {

        createLoan(BigDecimal.valueOf(8000), testData.creditor, testData.debtor, LocalDate.now().minusMonths(1), LocalDate.now().minusDays(15), new BigDecimal("0.15"), LoanStatus.ACTIVE);

        assertTrue(loanService.checkOverdueLoans());

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
