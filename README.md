This is a Java project that implements a P2P lending platform where users can lend money to each other.
The system supports basic financial operations such as creating loans, payments, displaying balances and transaction history.

Technologies:
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

Planned improvements:
- [ ] Complete implementation of concrete DAO classes (UserDaoImpl, LoanDaoImpl, PaymentDaoImpl)
- [ ] Implement services (LoanService, PaymentService) with validation and business logic support
- [ ] Add CriteriaBuilder usage for dynamic queries
- [ ] Build CLI or simple UI (JavaFX/Console)
- [ ] Implement search/filtering
- [ ] Loan status tracking: active, repaid, overdue
- [ ] Error handling
