package com.makeup.booking.model.exceptions;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(Long id) {
        super("Appointment with ID " + id + " not found");
    }
}
