package com.makeup.booking.service.impl;

import com.makeup.booking.model.User;
import com.makeup.booking.model.enums.Role;
import com.makeup.booking.model.exceptions.UserAlreadyExistsException;
import com.makeup.booking.model.exceptions.UserNotFoundException;
import com.makeup.booking.repository.UserRepository;
import com.makeup.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User findByFirstNameAndLastName(String firstName, String lastName) {
        return userRepository.findUserByFirstNameAndLastNameIgnoreCase(firstName, lastName).orElseThrow(() -> new UserNotFoundException(firstName, lastName));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(String firstName, String lastName, String email, String password, String phoneNumber, String profileImage, Role role) {
        if(firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("User information is incomplete");
        }
        User user = new User(firstName, lastName, email, password, phoneNumber, profileImage, true, role);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, String firstName, String lastName, String email, String password, String phoneNumber, String profileImage, Role role) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("User information is incomplete");
        }
        User user = getById(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhoneNumber(phoneNumber);
        user.setProfileImage(profileImage);
        user.setRole(role);

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = getById(id);
        userRepository.delete(user);
    }

    @Override
    public Role findRoleById(Long id) {
        return getById(id).getRole();
    }

    @Override
    public void toggleUserStatus(Long id) {
        User user = getById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (userRepository.existsByEmail(email)){
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }
        return false;
    }
}
