package mthree.stocksimulator.web;

import java.math.BigDecimal;
import mthree.stocksimulator.model.User;
import mthree.stocksimulator.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import mthree.stocksimulator.dto.ApiDtos.AuthRequest;
import mthree.stocksimulator.dto.ApiDtos.UserDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRestControllerTest {

    @Mock private UserServiceImpl userService;

    private UserRestController controller;

    @BeforeEach
    void setUp() {
        controller = new UserRestController(userService);
    }

    // ---- Registration ----

    @Test
    void register_createsNewUser() {
        User created = makeUser(1, "alice", "100000.00");
        when(userService.getUserByUserName("alice")).thenReturn(null);
        when(userService.createUser(eq("alice"), any(BigDecimal.class))).thenReturn(created);

        ResponseEntity<?> response = controller.register(new AuthRequest("alice", null, null));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserDto body = (UserDto) response.getBody();
        assertEquals("alice", body.userName());
        assertEquals(1, body.uid());
    }

    @Test
    void register_rejectsDuplicateUsername() {
        when(userService.getUserByUserName("alice")).thenReturn(makeUser(1, "alice", "100000.00"));

        ResponseEntity<?> response = controller.register(new AuthRequest("alice", null, null));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void register_rejectsBlankUsername() {
        ResponseEntity<?> response = controller.register(new AuthRequest("  ", null, null));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void register_rejectsNullUsername() {
        ResponseEntity<?> response = controller.register(new AuthRequest(null, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void register_usesCustomBalanceWhenProvided() {
        BigDecimal customBal = new BigDecimal("50000.00");
        User created = makeUser(1, "bob", "50000.00");
        when(userService.getUserByUserName("bob")).thenReturn(null);
        when(userService.createUser("bob", customBal)).thenReturn(created);

        ResponseEntity<?> response = controller.register(new AuthRequest("bob", null, customBal));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userService).createUser("bob", customBal);
    }

    // ---- Login ----

    @Test
    void login_returnsUserWhenFound() {
        when(userService.getUserByUserName("alice")).thenReturn(makeUser(1, "alice", "100000.00"));

        ResponseEntity<?> response = controller.login(new AuthRequest("alice", null, null));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDto body = (UserDto) response.getBody();
        assertEquals("alice", body.userName());
    }

    @Test
    void login_returns404WhenNotFound() {
        when(userService.getUserByUserName("nobody")).thenReturn(null);

        ResponseEntity<?> response = controller.login(new AuthRequest("nobody", null, null));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---- Get user ----

    @Test
    void getUser_returnsUserById() {
        when(userService.getUser(1)).thenReturn(makeUser(1, "alice", "100000.00"));

        ResponseEntity<?> response = controller.getUser(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDto body = (UserDto) response.getBody();
        assertEquals(1, body.uid());
        assertEquals("alice", body.userName());
    }

    @Test
    void getUser_returns404ForMissing() {
        when(userService.getUser(999))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException(1));

        ResponseEntity<?> response = controller.getUser(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---- Helper ----

    private User makeUser(int uid, String name, String balance) {
        User user = new User();
        user.setUid(uid);
        user.setUserName(name);
        user.setAccountBal(new BigDecimal(balance));
        return user;
    }
}
