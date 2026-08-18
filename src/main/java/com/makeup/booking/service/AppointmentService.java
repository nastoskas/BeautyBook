package com.makeup.booking.service;

import com.makeup.booking.model.Appointment;
import com.makeup.booking.model.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {
    Appointment getById(Long id);
    List<Appointment> findAll();
    List<Appointment> findByArtist(Long artistProfileId);
    List<Appointment> findByClient(Long clientId);
    List<Appointment> findByArtistAndDate(Long artistProfileId, LocalDate date);
    Appointment create(
            Long clientId,
            Long artistProfileId,
            LocalDate date,
            LocalTime startTime,
            List<Long> beautyServiceIds,
            String imagePath,
            String notes
    );
    Appointment updateStatus(Long id, AppointmentStatus status);
    Appointment reschedule(Long id, LocalDate date, LocalTime startTime);
    Appointment updateInspirationImage(Long id, String imagePath);
    Appointment removeInspirationImage(Long id);
    void cancel(Long id);
}
