package org.example.service;

import org.example.config.HibernateUtil;
import org.example.dao.UserDAO;
import org.example.dao.impl.UserDAOImpl;
import org.example.dto.UserDTO;
import org.example.model.User;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private static SessionFactory sessionFactory;
    private UserService userService;
    private static User cachedUser;

    @BeforeAll
    public static void setUpClass() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }

    @BeforeEach
    public void setUp() {
        UserDAO userDAO = new UserDAOImpl(sessionFactory);
        userService = new UserService(userDAO);
    }

    public User userData() {
        if (cachedUser == null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setFirstName("FirstName");
            userDTO.setLastName("LastName");
            userDTO.setPhoneNumber("PhoneNumber");
            userDTO.setEmail("Email");
            cachedUser = userService.createUser(userDTO);
        }
        return cachedUser;
    }

    @Test
    public void createValidUserTest() {
        User user = userData();
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("FirstName", user.getFirstName());
    }

    @Test
    public void createInvalidUserTest() {
        UserDTO userDTO = new UserDTO();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
        assertEquals("Invalid user creation data", exception.getMessage());
    }

    @Test
    public void updateUserFirstNameValidTest() {

        User user = userData();
        Long id = user.getId();

        UserDTO userDTOUpdate = new UserDTO();
        userDTOUpdate.setFirstName("FirstNameUpdate");
        User updateUser = userService.updateUser(id, userDTOUpdate);
        assertEquals("FirstNameUpdate", updateUser.getFirstName());
    }

    @Test
    public void updateInvalidUserIdTest() {
        UserDTO userDTOUpdate = new UserDTO();
        userDTOUpdate.setFirstName("FirstNameUpdate");
        Long id = 2L;
        Exception exception = assertThrows(IllegalStateException.class, () -> userService.updateUser(id, userDTOUpdate));
        assertEquals("User with id " + id + " not found", exception.getMessage());
    }

    @Test
    public void updateInvalidDataTest() {
        User user = userData();
        Long id = user.getId();

        UserDTO userDTOUpdate = new UserDTO();
        userDTOUpdate.setFirstName(" ");

        User updateUser = userService.updateUser(id, userDTOUpdate);
        assertEquals("FirstNameUpdate", updateUser.getFirstName());
    }

    @AfterAll
    public static void cleanup() {
        var session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        session.createQuery("DELETE FROM User").executeUpdate();
        tx.commit();
        session.close();
    }
}
