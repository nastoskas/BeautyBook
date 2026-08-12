package com.makeup.booking.model.exceptions;

public class ArtistProfileNotFoundException extends RuntimeException {
    public ArtistProfileNotFoundException(String message) {
        super(message);
    }
    public ArtistProfileNotFoundException(Long id){
        super("Artist profile with ID " + id + " not found.");
    }
}
