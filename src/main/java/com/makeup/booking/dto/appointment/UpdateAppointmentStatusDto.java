package com.makeup.booking.dto.appointment;

import com.makeup.booking.model.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAppointmentStatusDto {
    @NotNull
    private AppointmentStatus status;
}
