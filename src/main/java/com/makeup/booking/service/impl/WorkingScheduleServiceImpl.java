package com.makeup.booking.service.impl;

import com.makeup.booking.model.ArtistProfile;
import com.makeup.booking.model.WorkingSchedule;
import com.makeup.booking.model.exceptions.WorkingScheduleNotFoundException;
import com.makeup.booking.repository.WorkingScheduleRepository;
import com.makeup.booking.service.ArtistProfileService;
import com.makeup.booking.service.WorkingScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkingScheduleServiceImpl implements WorkingScheduleService {

    private final WorkingScheduleRepository workingScheduleRepository;
    private final ArtistProfileService artistProfileService;

    @Override
    public WorkingSchedule getById(Long id) {
        return workingScheduleRepository.findById(id).orElseThrow(() -> new WorkingScheduleNotFoundException(id));
    }

    @Override
    public List<WorkingSchedule> findByArtist(Long artistProfileId) {
        return workingScheduleRepository.findByArtistProfileId(artistProfileId);
    }

    @Override
    public WorkingSchedule create(
            Long artistProfileId,
            DayOfWeek day,
            LocalTime startTime,
            LocalTime endTime,
            boolean available) {

        if (day == null) {
            throw new IllegalArgumentException("Day is required");
        }

        if ((startTime == null) != (endTime == null)) {
            throw new IllegalArgumentException(
                    "Both start time and end time must be provided"
            );
        }

        if (startTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }

        ArtistProfile artistProfile =
                artistProfileService.getById(artistProfileId);

        if (available) {
            workingScheduleRepository
                    .findByArtistProfileIdAndDayAndAvailableTrue(
                            artistProfileId,
                            day
                    )
                    .ifPresent(schedule -> {
                        schedule.setAvailable(false);
                        workingScheduleRepository.save(schedule);
                    });
        }
        WorkingSchedule schedule =
                new WorkingSchedule(
                        day,
                        startTime,
                        endTime,
                        available,
                        artistProfile
                );

        return workingScheduleRepository.save(schedule);
    }

    @Override
    public WorkingSchedule update(Long id, DayOfWeek day, LocalTime startTime, LocalTime endTime, boolean available) {
        if (day == null) {
            throw new IllegalArgumentException("Day is required");
        }

        if ((startTime == null) != (endTime == null)) {
            throw new IllegalArgumentException(
                    "Both start time and end time must be provided"
            );
        }

        if (startTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }

        WorkingSchedule workingSchedule = getById(id);

        if (available) {
            workingScheduleRepository
                    .findByArtistProfileIdAndDayAndAvailableTrue(
                            workingSchedule.getArtistProfile().getId(),
                            day
                    )
                    .ifPresent(schedule -> {
                        if (!schedule.getId().equals(id)) {
                            schedule.setAvailable(false);
                            workingScheduleRepository.save(schedule);
                        }
                    });
        }

        workingSchedule.setDay(day);
        workingSchedule.setStartTime(startTime);
        workingSchedule.setEndTime(endTime);
        workingSchedule.setAvailable(available);
        return workingScheduleRepository.save(workingSchedule);
    }

    @Override
    public void delete(Long id) {
        WorkingSchedule workingSchedule = getById(id);
        workingScheduleRepository.delete(workingSchedule);
    }
}
