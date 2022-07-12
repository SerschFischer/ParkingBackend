package de.volkswagen.fakultaet.backend.controller;

import de.volkswagen.fakultaet.backend.domain.dto.UserUpdateRequest;
import de.volkswagen.fakultaet.backend.domain.model.User;
import de.volkswagen.fakultaet.backend.service.AuthService;
import de.volkswagen.fakultaet.backend.service.AuthService.TokenExpiredException;
import de.volkswagen.fakultaet.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private UserService userService;
    private AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<User> getUser(@RequestHeader(value = "Authorization") String token) {
        try {
            return ResponseEntity.accepted().body(this.authService.getCurrentUser(token));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        } catch (TokenExpiredException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PatchMapping
    public ResponseEntity<User> updateUser(@RequestHeader(value = "Authorization") String token,
                                           @RequestBody UserUpdateRequest updateRequest) {
        try {
            User currentUser = this.authService.getCurrentUser(token);
            return ResponseEntity.ok(this.userService.updateUser(currentUser, updateRequest));
        } catch (EntityNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        } catch (TokenExpiredException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
