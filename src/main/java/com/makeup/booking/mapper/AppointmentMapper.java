package com.makeup.booking.mapper;

import com.makeup.booking.dto.appointment.AppointmentResponseDto;
import com.makeup.booking.model.Appointment;
import com.makeup.booking.model.BeautyService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppointmentMapper {
    public AppointmentResponseDto toDto(Appointment appointment){
        AppointmentResponseDto dto = new AppointmentResponseDto();

        dto.setId(appointment.getId());
        dto.setDate(appointment.getAppointmentDate());
        dto.setStartTime(appointment.getAppointmentStartTime());
        dto.setEndTime(appointment.getAppointmentEndTime());
        dto.setPrice(appointment.getPrice());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setImagePath(appointment.getInspirationImagePath());

        if (appointment.getArtistProfile() != null && appointment.getArtistProfile().getUser() != null) {
            String artistName = appointment.getArtistProfile()
                    .getUser()
                    .getFirstName() + " " +
                    appointment.getArtistProfile()
                            .getUser()
                            .getLastName();
            dto.setArtistName(artistName);
        }

        List<String> services = appointment.getBeautyServices()
                .stream()
                .map(BeautyService::getName)
                .toList();
        dto.setServices(services);

        return dto;
    }
}
