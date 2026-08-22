package com.makeup.booking.controller;

import com.makeup.booking.dto.appointment.AppointmentResponseDto;
import com.makeup.booking.mapper.AppointmentMapper;
import com.makeup.booking.model.Appointment;
import com.makeup.booking.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
