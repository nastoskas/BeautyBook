package com.makeup.booking.service;

import com.makeup.booking.model.WorkingSchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface WorkingScheduleService {
    WorkingSchedule getById(Long id);
    List<WorkingSchedule> findByArtist(Long artistProfileId);
    WorkingSchedule findActiveByArtistAndDay(Long artistProfileId, DayOfWeek day);
    WorkingSchedule create(
            Long artistProfileId,
            DayOfWeek day,
            LocalTime startTime,
            LocalTime endTime,
            boolean available
    );
    WorkingSchedule update (
            Long id,
            DayOfWeek day,
            LocalTime startTime,
            LocalTime endTime,
            boolean available
    );
    void delete(Long id);
}
