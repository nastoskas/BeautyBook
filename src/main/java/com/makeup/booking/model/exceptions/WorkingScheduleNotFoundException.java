package com.makeup.booking.model.exceptions;

public class WorkingScheduleNotFoundException extends RuntimeException {
    public WorkingScheduleNotFoundException(Long id) {
        super("Working schedule with ID " + id + " not found");
    }
}
