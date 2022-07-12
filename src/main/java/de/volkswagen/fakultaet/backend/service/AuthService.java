package de.volkswagen.fakultaet.backend.service;

import de.volkswagen.fakultaet.backend.domain.dto.UserLoginRequest;
import de.volkswagen.fakultaet.backend.domain.dto.UserRegisterRequest;
import de.volkswagen.fakultaet.backend.domain.model.AuthUser;
import de.volkswagen.fakultaet.backend.domain.model.User;
import de.volkswagen.fakultaet.backend.repository.AuthUserRepository;
import de.volkswagen.fakultaet.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;

@Service
public class AuthService {
    public static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private AuthUserRepository authUserRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public AuthService(AuthUserRepository authUserRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerUser(UserRegisterRequest request) {
        if (this.userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EntityExistsException("Email already exists");
        }
        User userToCreate = new User();
        BeanUtils.copyProperties(request, userToCreate, "password");
        userToCreate.setPassword(passwordEncoder.encode(request.getPassword()));
        User userAdded = this.userRepository.save(userToCreate);
        AuthUser authUserToAdd = new AuthUser();
        authUserToAdd.setUserId(userAdded.getId());
        return this.authUserRepository.save(authUserToAdd).getToken();
    }

    public String loginUser(UserLoginRequest request) {
        User user = this.userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Incorrect Email"));
        if (!this.passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Incorrect Password");
        }
        AuthUser authUserToAdd = new AuthUser();
        authUserToAdd.setUserId(user.getId());
        return this.authUserRepository.save(authUserToAdd).getToken();
    }

    public void logoutUser(String token) {
        AuthUser authUserToLogout = this.authUserRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("User already logged out"));
        this.authUserRepository.delete(authUserToLogout);
    }

    public User getCurrentUser(String token) {
        AuthUser currentAuthUser = this.authUserRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("User not logged in"));
        // Check if token expired and delete AuthUser
        if (currentAuthUser.getExpiredAt().isBefore(LocalDateTime.now())) {
            this.authUserRepository.delete(currentAuthUser);
            throw new TokenExpiredException("Token expired");
        }
        return this.userRepository.findById(currentAuthUser.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User does not exists"));
    }

    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException() {
            super();
        }

        public InvalidPasswordException(String message) {
            super(message);
        }
    }

    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException() {
            super();
        }

        public TokenExpiredException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException() {
            super();
        }

        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
