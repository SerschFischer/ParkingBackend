package de.volkswagen.fakultaet.backend.service;

import de.volkswagen.fakultaet.backend.domain.dto.UserRegisterRequest;
import de.volkswagen.fakultaet.backend.domain.dto.UserUpdateRequest;
import de.volkswagen.fakultaet.backend.domain.model.User;
import de.volkswagen.fakultaet.backend.repository.UserRepository;
import de.volkswagen.fakultaet.backend.service.AuthService.InvalidPasswordException;
import de.volkswagen.fakultaet.backend.utilities.ApplicationUtilities;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(UserRegisterRequest request) {
        User userToCreate = new User();
        BeanUtils.copyProperties(request, userToCreate, "password");
        userToCreate.setPassword(this.passwordEncoder.encode(request.getPassword()));
        return this.userRepository.save(userToCreate);
    }

    public User updateUser(User currentUser, UserUpdateRequest updateRequest) {
        List<String> ignoreProperties = ApplicationUtilities.getNullPropertyNamesAsList(updateRequest);
        ignoreProperties.addAll(Arrays.asList("password", "oldPassword", "newPassword"));
        BeanUtils.copyProperties(updateRequest, currentUser, ignoreProperties.toArray(new String[0]));
        if (updateRequest.getOldPassword() != null && updateRequest.getNewPassword() != null) {
            if (this.passwordEncoder.matches(updateRequest.getOldPassword(), currentUser.getPassword())) {
                currentUser.setPassword(this.passwordEncoder.encode(updateRequest.getNewPassword()));
            } else {
                throw new InvalidPasswordException();
            }
        }
        return this.userRepository.save(currentUser);
    }
}


