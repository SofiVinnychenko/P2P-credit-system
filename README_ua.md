Це Java-проєкт, що реалізує P2P-кредитну платформу, в якій користувачі можуть видавати кредити один одному. 
Система підтримує базові фінансові операції, такі як створення позик, виплати, відображення залишків і історії транзакцій.

Технології
- Java 21
- Hibernate ORM
- MySQL
- Maven
- Lombok
- JDBC/Hibernate transaction management

Реалізовано:
- [x] Конфігурація Hibernate через `hibernate.cfg.xml`
- [x] Підключення до MySQL
- [x] Entity-класи для `User`, `Loan`, `Payment`
- [x] Сесії та транзакції Hibernate
- [x] `HibernateUtil` — централізований доступ до `SessionFactory`
- [x] `AppContext` — ручне керування залежностями
- [x] Базовий CRUD через `AbstractQueriesDAO<T>`

Плановані доопрацювання
- [ ] Завершити реалізацію конкретних DAO-класів (`UserDaoImpl`, `LoanDaoImpl`, `PaymentDaoImpl`)
- [ ] Реалізувати сервіси (`LoanService`, `PaymentService`) з підтримкою валідації та бізнес-логіки
- [ ] Додати використання CriteriaBuilder для динамічних запитів
- [ ] Побудувати CLI або простий UI (JavaFX/Console)
- [ ] Реалізувати пошук/фільтрацію
- [ ] Облік статусу позики: активна, погашена, прострочена
- [ ] Обробка помилок
