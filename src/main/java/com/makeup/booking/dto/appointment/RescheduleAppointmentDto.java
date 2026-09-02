package com.makeup.booking.dto.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class RescheduleAppointmentDto {
    @NotNull
    private LocalDate newDate;
    @NotNull
    private LocalTime newStartTime;
}
