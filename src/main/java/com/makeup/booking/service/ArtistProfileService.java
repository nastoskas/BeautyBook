package com.makeup.booking.service;

import com.makeup.booking.model.ArtistProfile;

import java.util.List;

public interface ArtistProfileService {
    ArtistProfile getById(Long id);
    List<ArtistProfile> findAll();
    ArtistProfile findByUserId(Long userId);
    ArtistProfile create(Long userId, String biography, String experience, String instagram);
    ArtistProfile update(Long id, String biography, String experience, String instagram);
    void delete(Long id);
}
