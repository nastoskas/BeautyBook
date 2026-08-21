package com.makeup.booking.dto.appointment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateAppointmentDto {
    @NotNull
    private Long artistId;
    @NotNull
    private LocalDate date;
    @NotNull
    private LocalTime startTime;
    @NotEmpty
    private List<Long> beautyServiceIds;
    private String imagePath;
    private String notes;
}
