package com.makeup.booking.service;

import com.makeup.booking.model.User;
import com.makeup.booking.model.enums.Role;

import java.util.List;

public interface UserService {
    User findByEmail(String email);
    User getById(Long id);
    User findByFirstNameAndLastName(String firstName, String lastName);
    List<User> findAll();
    User registerUser(String firstName, String lastName, String email, String password, String phoneNumber, String profileImage, Role role);
    User updateUser(Long id, String firstName, String lastName, String email, String password, String phoneNumber, String profileImage, Role role);
    void deleteUser(Long id);
    Role findRoleById(Long id);
    void toggleUserStatus(Long id);
    boolean existsByEmail(String email);
}
