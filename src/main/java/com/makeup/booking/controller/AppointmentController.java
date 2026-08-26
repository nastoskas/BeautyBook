package com.makeup.booking.controller;

import com.makeup.booking.dto.appointment.AppointmentResponseDto;
import com.makeup.booking.dto.appointment.CreateAppointmentDto;
import com.makeup.booking.dto.appointment.UpdateAppointmentStatusDto;
import com.makeup.booking.mapper.AppointmentMapper;
import com.makeup.booking.model.Appointment;
import com.makeup.booking.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping
    public List<AppointmentResponseDto> findAll() {
        return appointmentService.findAll()
                .stream()
                .map(appointmentMapper::toDto)
                .toList();
    }

    @GetMapping("/artist/{artistId}")
    List<AppointmentResponseDto> findByArtist(@PathVariable Long artistId) {
        return appointmentService.findByArtist(artistId)
                .stream()
                .map(appointmentMapper::toDto)
                .toList();
    }

    @GetMapping("/client/{clientId}")
    List<AppointmentResponseDto> findByClient(@PathVariable Long clientId) {
        return appointmentService.findByClient(clientId)
                .stream()
                .map(appointmentMapper::toDto)
                .toList();
    }

    @GetMapping("/artist/{artistId}/date/{date}")
    List<AppointmentResponseDto> findByArtistAndDate (@PathVariable Long artistId,
                                                      @PathVariable LocalDate date) {
        return appointmentService.findByArtistAndDate(artistId, date)
                .stream()
                .map(appointmentMapper::toDto)
                .toList();
    }

    @PatchMapping("/{id}/status")
    public AppointmentResponseDto updateStatus(@PathVariable Long id,
                                               @Valid @RequestBody UpdateAppointmentStatusDto dto) {
        return appointmentMapper.toDto(appointmentService.updateStatus(id, dto.getStatus()));
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponseDto cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return appointmentMapper.toDto(appointmentService.getById(id));
    }

    @DeleteMapping("/{id}/inspiration-image")
    public AppointmentResponseDto removeInspirationImage(@PathVariable Long id){
        appointmentService.removeInspirationImage(id);
        return appointmentMapper.toDto(appointmentService.getById(id));
    }
}
