package com.makeup.booking.model.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("User with email " + email + " not found");
    }
    public UserNotFoundException(Long id) {
        super("User with ID " + id + " not found");
    }
    public UserNotFoundException(String firstName, String lastName) {
        super("User with name " + firstName + " " + lastName + " not found");
    }
}
