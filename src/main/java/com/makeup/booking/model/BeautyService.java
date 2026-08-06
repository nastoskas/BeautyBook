package com.makeup.booking.model;

import com.makeup.booking.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "beauty_services")
public class BeautyService extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;
    private String imagePath;
    @NotBlank
    @Column(length = 1000)
    private String description;
    @Column(nullable = false)
    @Positive(message = "Duration must be a greater than zero")
    private int duration; // in minutes
    @NotNull
    @Positive(message = "Price must be a positive value")
    private BigDecimal price;
    @Column(nullable = false)
    private boolean active = true;
}
