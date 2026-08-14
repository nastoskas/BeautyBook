package com.makeup.booking.repository;

import com.makeup.booking.model.ArtistProfile;
import com.makeup.booking.model.WorkingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingScheduleRepository extends JpaRepository<WorkingSchedule, Long> {
    List<WorkingSchedule> findByArtistProfileId(Long artistProfileId);
    Optional<WorkingSchedule> findByArtistProfileIdAndDayAndAvailableTrue(Long artistProfileId, DayOfWeek day);
}
