package com.makeup.booking.service.impl;

import com.makeup.booking.model.BeautyService;
import com.makeup.booking.model.exceptions.BeautyServiceNotFoundException;
import com.makeup.booking.repository.BeautyServiceRepository;
import com.makeup.booking.service.BeautyServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BeautyServiceServiceImpl implements BeautyServiceService {
    private final BeautyServiceRepository beautyServiceRepository;

    @Override
    public BeautyService getById(Long id) {
        return beautyServiceRepository.findById(id).orElseThrow(() -> new BeautyServiceNotFoundException(id));
    }

    @Override
    public List<BeautyService> findAll() {
        return beautyServiceRepository.findAll();
    }

    @Override
    public List<BeautyService> findAllActive() {
        return beautyServiceRepository.findAllByActiveTrue();
    }

    @Override
    public BeautyService create(String name, String imagePath, String description, int duration, BigDecimal price) {
        validateParameters(name, description, duration, price);
        BeautyService beautyService = new BeautyService();
        beautyService.setName(name);
        beautyService.setImagePath(imagePath);
        beautyService.setDescription(description);
        beautyService.setDuration(duration);
        beautyService.setPrice(price);
        beautyService.setActive(true);
        return beautyServiceRepository.save(beautyService);
    }

    @Override
    public BeautyService update(Long id, String name, String imagePath, String description, int duration, BigDecimal price) {
        BeautyService beautyService = getById(id);
        validateParameters(name, description, duration, price);
        beautyService.setName(name);
        beautyService.setImagePath(imagePath);
        beautyService.setDescription(description);
        beautyService.setDuration(duration);
        beautyService.setPrice(price);
        return beautyServiceRepository.save(beautyService);
    }

    private void validateParameters(String name, String description, int duration, BigDecimal price) {
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("Service name is required");
        }
        if (description == null || description.isBlank()){
            throw new IllegalArgumentException("Service description is required");
        }
        if (duration <= 0){
            throw new IllegalArgumentException("Service duration must be greater than zero");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Service price cannot be negative");
        }
    }

    @Override
    public void delete(Long id) {
        BeautyService beautyService = getById(id);
        beautyServiceRepository.delete(beautyService);
    }

    @Override
    public void toggleServiceStatus(Long id) {
        BeautyService beautyService = getById(id);
        beautyService.setActive(!beautyService.isActive());
        beautyServiceRepository.save(beautyService);
    }
}
