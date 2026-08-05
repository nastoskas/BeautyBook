package com.makeup.booking.model;

import com.makeup.booking.common.entity.BaseEntity;
import com.makeup.booking.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;
    @NotBlank
    @Size(min = 8, max = 100, message = "Password is required and must be minimum 8 characters long")
    private String password;
    @NotBlank
    @Column(nullable = false)
    private String phoneNumber;
    private String profileImage;
    @Column(nullable = false)
    private boolean enabled = false;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
