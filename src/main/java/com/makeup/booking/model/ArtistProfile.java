package com.makeup.booking.model;

import com.makeup.booking.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "artist_profiles")
public class ArtistProfile extends BaseEntity {
    @NotBlank
    private String biography;
    @NotBlank
    private String experience;
    private String instagram;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @OneToMany(mappedBy = "artistProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkingSchedule> workingSchedules = new ArrayList<>();
    @OneToMany(mappedBy = "artistProfile")
    private List<Appointment> appointments = new ArrayList<>();
}
