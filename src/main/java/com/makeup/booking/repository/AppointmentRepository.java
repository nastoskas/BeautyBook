package com.makeup.booking.repository;

import com.makeup.booking.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByArtistProfileId(Long artistProfileId);
    List<Appointment> findByClientId(Long clientId);
    List<Appointment> findByArtistProfileIdAndAppointmentDate(Long artistProfileId, LocalDate date);
}
