package com.makeup.booking.model.exceptions;

public class BeautyServiceNotFoundException extends RuntimeException {
    public BeautyServiceNotFoundException(Long id) {
        super("Beauty service with ID " + id + " not found");
    }
}
