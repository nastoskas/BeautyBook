package com.makeup.booking.controller;

import com.makeup.booking.dto.appointment.AppointmentResponseDto;
import com.makeup.booking.dto.appointment.CreateAppointmentDto;
import com.makeup.booking.mapper.AppointmentMapper;
import com.makeup.booking.model.Appointment;
import com.makeup.booking.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    @GetMapping("/{id}")
    public AppointmentResponseDto getById(@PathVariable Long id){

        Appointment appointment = appointmentService.getById(id);

        return appointmentMapper.toDto(appointment);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponseDto createAppointment(@Valid @RequestBody CreateAppointmentDto dto){
        Appointment appointment = appointmentService.create(
                dto.getClientId(),
                dto.getArtistId(),
                dto.getDate(),
                dto.getStartTime(),
                dto.getBeautyServiceIds(),
                dto.getImagePath(),
                dto.getNotes()
        );
        return appointmentMapper.toDto(appointment);
    }
}
