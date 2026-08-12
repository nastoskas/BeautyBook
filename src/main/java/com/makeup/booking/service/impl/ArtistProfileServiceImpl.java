package com.makeup.booking.service.impl;

import com.makeup.booking.model.ArtistProfile;
import com.makeup.booking.model.exceptions.ArtistProfileNotFoundException;
import com.makeup.booking.repository.ArtistProfileRepository;
import com.makeup.booking.service.ArtistProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistProfileServiceImpl implements ArtistProfileService {

    private final ArtistProfileRepository artistProfileRepository;

    @Override
    public ArtistProfile getById(Long id) {
        return artistProfileRepository.findById(id).orElseThrow(() -> new ArtistProfileNotFoundException(id));
    }

    @Override
    public List<ArtistProfile> findAll() {
        return artistProfileRepository.findAll();
    }

    @Override
    public ArtistProfile findByUserId(Long userId) {
        return artistProfileRepository.findByUserId(userId).orElseThrow(() -> new ArtistProfileNotFoundException("Artist profile for user with ID " + userId + " not found"));
    }
}
