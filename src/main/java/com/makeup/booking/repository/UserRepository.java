package com.makeup.booking.repository;

import com.makeup.booking.model.User;
import com.makeup.booking.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findUserByFirstNameAndLastNameIgnoreCase(String firstName, String lastName);

    boolean existsByEmail(String email);
}
