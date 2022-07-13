package de.volkswagen.fakultaet.backend.controller;

import de.volkswagen.fakultaet.backend.domain.dto.UserLoginRequest;
import de.volkswagen.fakultaet.backend.domain.dto.UserRegisterRequest;
import de.volkswagen.fakultaet.backend.service.AuthService;
import de.volkswagen.fakultaet.backend.service.AuthService.InvalidPasswordException;
import de.volkswagen.fakultaet.backend.service.AuthService.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegisterRequest request) {
        try {
            return ResponseEntity.accepted().body(this.authService.registerUser(request));
        } catch (EntityExistsException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserLoginRequest request) {
        try {
            return ResponseEntity.accepted().body(this.authService.loginUser(request));
        } catch (EntityNotFoundException | InvalidPasswordException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader(value = "Authorization") String token) {
        try {
            this.authService.logoutUser(token);
            return ResponseEntity.accepted().build();
        } catch (UnauthorizedException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}
