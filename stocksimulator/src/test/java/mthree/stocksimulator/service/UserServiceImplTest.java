package mthree.stocksimulator.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import mthree.stocksimulator.dao.UserDao;
import mthree.stocksimulator.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserDao userDao;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao);
    }

    @Test
    void createUser_setsBalanceAndDelegatesToDao() {
        when(userDao.createUser(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUid(1);
            return u;
        });

        User result = userService.createUser("alice", new BigDecimal("50000"));

        assertEquals("alice", result.getUserName());
        assertEquals(new BigDecimal("50000.00"), result.getAccountBal());
        assertEquals(1, result.getUid());
    }

    @Test
    void createUser_scalesBalanceTo2Decimals() {
        when(userDao.createUser(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUid(2);
            return u;
        });

        User result = userService.createUser("bob", new BigDecimal("1234.5678"));

        // Should be rounded to 2 decimal places
        assertEquals(new BigDecimal("1234.57"), result.getAccountBal());
    }

    @Test
    void getUser_delegatesToDao() {
        User user = new User();
        user.setUid(1);
        user.setUserName("alice");
        when(userDao.getUser(1)).thenReturn(user);

        User result = userService.getUser(1);

        assertEquals("alice", result.getUserName());
        verify(userDao).getUser(1);
    }

    @Test
    void getUserByUserName_returnsNullWhenNotFound() {
        when(userDao.getUserByUserName("unknown")).thenReturn(null);

        assertNull(userService.getUserByUserName("unknown"));
    }

    @Test
    void getUserByUserName_returnsUserWhenFound() {
        User user = new User();
        user.setUid(1);
        user.setUserName("alice");
        when(userDao.getUserByUserName("alice")).thenReturn(user);

        User result = userService.getUserByUserName("alice");

        assertNotNull(result);
        assertEquals("alice", result.getUserName());
    }

    @Test
    void getAllUsers_delegatesToDao() {
        User u1 = new User(); u1.setUid(1); u1.setUserName("alice");
        User u2 = new User(); u2.setUid(2); u2.setUserName("bob");
        when(userDao.getAllUsers()).thenReturn(Arrays.asList(u1, u2));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }
}
