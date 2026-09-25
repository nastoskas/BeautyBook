package com.makeup.booking;

import com.makeup.booking.model.ArtistProfile;
import com.makeup.booking.model.WorkingSchedule;
import com.makeup.booking.model.exceptions.WorkingScheduleNotFoundException;
import com.makeup.booking.repository.WorkingScheduleRepository;
import com.makeup.booking.service.ArtistProfileService;
import com.makeup.booking.service.WorkingScheduleService;
import com.makeup.booking.service.impl.WorkingScheduleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkingScheduleServiceImplTest {

    @Mock
    private WorkingScheduleRepository workingScheduleRepository;

    @Mock
    private ArtistProfileService artistProfileService;

    @InjectMocks
    private WorkingScheduleServiceImpl workingScheduleService;

    @Test
    public void getById_ShouldReturnWorkingSchedule() {
        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setId(1L);

        when(workingScheduleRepository.findById(1L)).thenReturn(Optional.of(workingSchedule));

        WorkingSchedule result = workingScheduleService.getById(1L);

        assertEquals(1L, result.getId());
        verify(workingScheduleRepository).findById(1L);
    }

    @Test
    public void getById_ShouldThrowExceptionWhenNotFound() {
        when(workingScheduleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(WorkingScheduleNotFoundException.class,
                () -> workingScheduleService.getById(1L));

        verify(workingScheduleRepository).findById(1L);
    }


}