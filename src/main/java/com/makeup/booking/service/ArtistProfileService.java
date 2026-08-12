package com.makeup.booking.service;

import com.makeup.booking.model.ArtistProfile;

import java.util.List;

public interface ArtistProfileService {
    ArtistProfile getById(Long id);
    List<ArtistProfile> findAll();
    ArtistProfile findByUserId(Long userId);
}
