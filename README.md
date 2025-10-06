Here's the translation with preserved formatting:

This is a Java project that implements a P2P lending platform where users can issue loans to each other. 
The system supports basic financial operations such as creating loans, payments, displaying balances, and calculating payment schedules when creating a loan with specific dates for each repayment. The created platform is based on a differentiated loan repayment system.

Technologies
- Java 21
- Hibernate ORM
- MySQL
- Maven
- Lombok
- JDBC/Hibernate transaction management

Implemented:
- [x] Hibernate configuration via hibernate.cfg.xml
- [x] MySQL connection
- [x] Entity classes for User, Loan, Payment
- [x] Hibernate sessions and transactions
- [x] HibernateUtil — centralized access to SessionFactory
- [x] AppContext — manual dependency management
- [x] Basic CRUD via AbstractQueriesDAO<T>
- [x] DTOs for Loan and User entities with field validation
- [x] enumeration for loan statuses: ACTIVE, REPAID, DEFAULTED
- [x] enumeration for payment statuses: PENDING, PAID
- [x] specific DAO classes.
- LoanDao: methods described for retrieving loans by lender; loans by debtor and loan status; sum of all loans by debtor; retrieving outstanding loans expiring in 7 days; retrieving all loans by status.
- PaymentDao: retrieving payments by loan and status; retrieving all payments by status.
- UserDao: retrieving users by keyword; retrieving all loans of a specific lender; retrieving all loans of a specific debtor
- [x] specific services.
- LoanService: creating a loan using LoanDTO and generating a payment schedule for the created loan; changing the status of a specific loan; closing a specific loan; retrieving active loans of a lender opened in the current year; calculating the net debt balance for a specific loan; calculating interest accrued on the remaining debt amount for the current payment; checking active loans and setting DEFAULTED status on overdue ones; checking whether a specific debtor can take a new loan.
- PaymentService: creating a payment schedule based on created loan data, with interest calculation for each payment; recalculating the amount of outstanding payments when repaying one for a specific loan; repaying a payment with a call to the recalculation method; retrieving the repayment date of the last payment for a specific loan; calculating the remaining principal net amount.
- UserService: creating a user using UserDTO; changing user information with field validation.
- [x] unit tests to verify functionality of LoanDAO, UserDAO, LoanService, UserService
      
Planned improvements:
- [ ] Complete writing unit tests for methods of PaymentDAO, PaymentService classes
