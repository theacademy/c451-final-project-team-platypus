package mthree.stocksimulator.web;

import java.math.BigDecimal;
import mthree.stocksimulator.dto.ApiDtos.AuthRequest;
import mthree.stocksimulator.dto.ApiDtos.UserDto;
import mthree.stocksimulator.model.User;
import mthree.stocksimulator.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User registration, login (username-only), and lookup.
 *
 * NOTE: auth here is intentionally minimal to match the existing schema, which
 * has no password or email column. The password field on AuthRequest is ignored.
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    // Default starting balance for new accounts (frontend register form has no balance field).
    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal("100000.00");

    private final UserServiceImpl userService;

    public UserRestController(UserServiceImpl userService) {
        this.userService = userService;
    }

    /** Register a new account. Returns 409 if the username already exists. */
    @PostMapping
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        if (req.userName() == null || req.userName().isBlank()) {
            return ResponseEntity.badRequest().body("userName is required");
        }
        if (userService.getUserByUserName(req.userName()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
        }
        BigDecimal balance = req.startingBalance() != null ? req.startingBalance() : DEFAULT_BALANCE;
        User created = userService.createUser(req.userName(), balance);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    /** Username-only login. Returns 404 if no such user. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        User user = userService.getUserByUserName(req.userName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user with that name");
        }
        return ResponseEntity.ok(toDto(user));
    }

    @GetMapping("/{uid}")
    public ResponseEntity<?> getUser(@PathVariable int uid) {
        try {
            return ResponseEntity.ok(toDto(userService.getUser(uid)));
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user with uid " + uid);
        }
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getUid(), u.getUserName(), u.getAccountBal());
    }
}