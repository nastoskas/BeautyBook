package com.makeup.booking.service;

import com.makeup.booking.model.BeautyService;

import java.math.BigDecimal;
import java.util.List;

public interface BeautyServiceService {
    BeautyService getById(Long id);
    List<BeautyService> findAll();
    List<BeautyService> findAllActive();
    BeautyService create(String name, String imagePath, String description, int duration, BigDecimal price);
    BeautyService update(Long id, String name, String imagePath, String description, int duration, BigDecimal price);
    void delete(Long id);
    void toggleServiceStatus(Long id);
}
