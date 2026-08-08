package com.makeup.booking.repository;

import com.makeup.booking.model.BeautyService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeautyServiceRepository extends JpaRepository<BeautyService, Long> {
}
