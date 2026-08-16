package com.makeup.booking.service.impl;

import com.makeup.booking.model.ArtistProfile;
import com.makeup.booking.model.User;
import com.makeup.booking.model.enums.Role;
import com.makeup.booking.model.exceptions.ArtistProfileNotFoundException;
import com.makeup.booking.repository.ArtistProfileRepository;
import com.makeup.booking.service.ArtistProfileService;
import com.makeup.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistProfileServiceImpl implements ArtistProfileService {

    private final ArtistProfileRepository artistProfileRepository;
    private final UserService userService;

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

    @Override
    public ArtistProfile create(Long userId, String biography, String experience, String instagram) {
        User user = userService.getById(userId);
        if (user.getRole() != Role.ARTIST){
            throw new IllegalArgumentException("Only users with ARTIST role can have an artist profile");
        }
        if (artistProfileRepository.findByUserId(userId).isPresent()){
            throw new IllegalArgumentException("This user already has an artist profile");
        }
        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setUser(user);
        artistProfile.setBiography(biography);
        artistProfile.setInstagram(instagram);
        return artistProfileRepository.save(artistProfile);
    }

    @Override
    public ArtistProfile update(Long id, String biography, String experience, String instagram) {
        ArtistProfile artistProfile = getById(id);
        artistProfile.setBiography(biography);
        artistProfile.setExperience(experience);
        artistProfile.setInstagram(instagram);
        return artistProfileRepository.save(artistProfile);
    }

    @Override
    public void delete(Long id) {
        ArtistProfile artistProfile = getById(id);
        artistProfileRepository.delete(artistProfile);
    }
}
