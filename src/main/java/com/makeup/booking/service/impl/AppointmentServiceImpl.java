package com.makeup.booking.service.impl;

import com.makeup.booking.model.*;
import com.makeup.booking.model.enums.AppointmentStatus;
import com.makeup.booking.model.enums.Role;
import com.makeup.booking.model.exceptions.AppointmentNotFoundException;
import com.makeup.booking.repository.AppointmentRepository;
import com.makeup.booking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final ArtistProfileService artistProfileService;
    private final WorkingScheduleService workingScheduleService;
    private final BeautyServiceService beautyServiceService;

    @Override
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id).orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    @Override
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    @Override
    public List<Appointment> findByArtist(Long artistProfileId) {
        return appointmentRepository.findByArtistProfileId(artistProfileId);
    }

    @Override
    public List<Appointment> findByClient(Long clientId) {
        return appointmentRepository.findByClientId(clientId);
    }

    @Override
    public List<Appointment> findByArtistAndDate(Long artistProfileId, LocalDate date) {
        return appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date);
    }

    @Override
    public Appointment create(Long clientId, Long artistProfileId, LocalDate date, LocalTime startTime, List<Long> beautyServiceIds, String imagePath,String notes) {
        if (date == null){
            throw new IllegalArgumentException("Appointment date is required");
        }
        if (startTime == null){
            throw new IllegalArgumentException("Appointment start time is required");
        }
        if (beautyServiceIds == null || beautyServiceIds.isEmpty()){
            throw new IllegalArgumentException("At least one beauty service is required");
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (date.isBefore(today) || (date.isEqual(today) && startTime.isBefore(now))){
            throw new IllegalArgumentException("Appointment date cannot be in the past");
        }

        User client = userService.getById(clientId);

        if (client.getRole() != Role.CLIENT){
            throw new IllegalArgumentException("Only clients can create appointments");
        }

        ArtistProfile artistProfile = artistProfileService.getById(artistProfileId);
        WorkingSchedule workingSchedule = workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek());

        if (workingSchedule.getStartTime() == null || workingSchedule.getEndTime() == null) {
            throw new IllegalArgumentException("Artist does not work on this day");
        }

        List<BeautyService> beautyServices =
                beautyServiceIds.stream()
                        .map(beautyServiceService::getById)
                        .peek(service -> {
                            if (!service.isActive()) {
                                throw new IllegalArgumentException(
                                        "Beauty service " +
                                                service.getId() +
                                                " is not active"
                                );
                            }
                        })
                        .toList();

        int totalDuration = beautyServices.stream()
                .mapToInt(BeautyService::getDuration)
                .sum();
        LocalTime endTime =
                startTime.plusMinutes(totalDuration);

        if (startTime.isBefore(workingSchedule.getStartTime()) || endTime.isAfter(workingSchedule.getEndTime())) {
            throw new IllegalArgumentException("Appointment is outside artist working hours");
        }

        List<Appointment> existingAppointments = appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date);

        boolean overlaps = existingAppointments.stream()
                .anyMatch(existing ->
                        startTime.isBefore(existing.getAppointmentEndTime())
                                && endTime.isAfter(
                                existing.getAppointmentStartTime()
                        )
                );
        if (overlaps) {
            throw new IllegalArgumentException(
                    "The selected time slot is already booked"
            );
        }

        BigDecimal totalPrice = beautyServices.stream()
                .map(BeautyService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Appointment appointment = new Appointment();

        appointment.setInspirationImagePath(imagePath);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentStartTime(startTime);
        appointment.setAppointmentEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setPrice(totalPrice);
        appointment.setNotes(notes);
        appointment.setBeautyServices(beautyServices);
        appointment.setArtistProfile(artistProfile);
        appointment.setClient(client);

        return appointmentRepository.save(appointment);
    }

    private boolean isValidStatusTransition(
            AppointmentStatus currentStatus,
            AppointmentStatus newStatus) {

        return switch (currentStatus) {
            case PENDING ->
                    newStatus == AppointmentStatus.CONFIRMED
                            || newStatus == AppointmentStatus.CANCELLED;

            case CONFIRMED ->
                    newStatus == AppointmentStatus.COMPLETED
                            || newStatus == AppointmentStatus.CANCELLED
                            || newStatus == AppointmentStatus.NO_SHOW;

            case CANCELLED, COMPLETED, NO_SHOW ->
                    false;
        };
    }

    @Override
    public Appointment updateStatus(Long id, AppointmentStatus status) {
        if (status == null){
            throw new IllegalArgumentException("Appointment status is required");
        }
        Appointment appointment = getById(id);
        AppointmentStatus appointmentStatus = appointment.getStatus();
        if (!isValidStatusTransition(appointmentStatus, status)){
            throw new IllegalArgumentException("Cannot change appointment status from " + appointmentStatus
            + " to " + status);
        }
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment reschedule(Long id, LocalDate date, LocalTime startTime) {
        Appointment appointment = getById(id);
        if (date == null){
            throw new IllegalArgumentException("Appointment date is required");
        }
        if (startTime == null){
            throw new IllegalArgumentException("Appointment start time is required");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cancelled appointment cannot be rescheduled"
            );
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Completed appointment cannot be rescheduled"
            );
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (date.isBefore(today) ||
                (date.isEqual(today) && startTime.isBefore(now))) {

            throw new IllegalArgumentException(
                    "Appointment date and time cannot be in the past"
            );
        }

        WorkingSchedule workingSchedule = workingScheduleService.findActiveByArtistAndDay(
                        appointment.getArtistProfile().getId(),
                        date.getDayOfWeek()
                );
        if (workingSchedule.getStartTime() == null || workingSchedule.getEndTime() == null) {
            throw new IllegalArgumentException("Artist does not work on this day");
        }

        int totalDuration = appointment.getBeautyServices()
                .stream()
                .mapToInt(BeautyService::getDuration)
                .sum();
        LocalTime endTime =
                startTime.plusMinutes(totalDuration);

        if (startTime.isBefore(workingSchedule.getStartTime()) || endTime.isAfter(workingSchedule.getEndTime())) {
            throw new IllegalArgumentException("Appointment is outside artist working hours");
        }

        List<Appointment> existingAppointments =
                appointmentRepository.findByArtistProfileIdAndAppointmentDate(
                                appointment.getArtistProfile().getId(),
                                date
                        );
        boolean overlaps = existingAppointments.stream()
                .filter(existing -> !existing.getId().equals(id))
                .anyMatch(existing ->
                        startTime.isBefore(
                                existing.getAppointmentEndTime())
                                && endTime.isAfter(existing.getAppointmentStartTime())
                );
        if (overlaps) {
            throw new IllegalArgumentException(
                    "The selected time slot is already booked"
            );
        }

        appointment.setAppointmentDate(date);
        appointment.setAppointmentStartTime(startTime);
        appointment.setAppointmentEndTime(endTime);

        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment updateInspirationImage(Long id, String imagePath) {
        Appointment appointment = getById(id);
        appointment.setInspirationImagePath(imagePath);
        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment removeInspirationImage(Long id) {
        Appointment appointment = getById(id);
        appointment.setInspirationImagePath(null);
        return appointmentRepository.save(appointment);
    }

    @Override
    public void cancel(Long id) {
        Appointment appointment = getById(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Appointment is already cancelled");
        }
        updateStatus(id, AppointmentStatus.CANCELLED);
    }
}
