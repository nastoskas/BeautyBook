package com.makeup.booking;

import com.makeup.booking.model.*;
import com.makeup.booking.model.enums.AppointmentStatus;
import com.makeup.booking.model.enums.Role;
import com.makeup.booking.model.exceptions.BeautyServiceNotFoundException;
import com.makeup.booking.repository.AppointmentRepository;
import com.makeup.booking.service.ArtistProfileService;
import com.makeup.booking.service.BeautyServiceService;
import com.makeup.booking.service.UserService;
import com.makeup.booking.service.WorkingScheduleService;
import com.makeup.booking.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceImplTest {
    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserService userService;

    @Mock
    private ArtistProfileService artistProfileService;

    @Mock
    private WorkingScheduleService workingScheduleService;

    @Mock
    private BeautyServiceService beautyServiceService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void create_shouldCreateAppointmentSuccessfully() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);

        User client = new User();
        client.setId(clientId);
        client.setRole(Role.CLIENT);
        when(userService.getById(clientId)).thenReturn(client);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(artistProfileId);
        when(artistProfileService.getById(artistProfileId))
                .thenReturn(artistProfile);

        WorkingSchedule workingSchedule = new WorkingSchedule();

        workingSchedule.setStartTime(LocalTime.of(8, 0));
        workingSchedule.setEndTime(LocalTime.of(15, 0));
        workingSchedule.setAvailable(true);
        when(workingScheduleService.findActiveByArtistAndDay(
                artistProfileId,
                date.getDayOfWeek()
        )).thenReturn(workingSchedule);

        BeautyService beautyService = new BeautyService();

        beautyService.setId(1L);
        beautyService.setDuration(60);
        beautyService.setPrice(new BigDecimal("1500"));
        beautyService.setActive(true);
        when(beautyServiceService.getById(1L))
                .thenReturn(beautyService);

        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(
                artistProfileId,
                date
        )).thenReturn(List.of());

        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.create(
                clientId,
                artistProfileId,
                date,
                startTime,
                List.of(1L),
                null,
                null
        );

        assertNotNull(result);
        assertEquals(date, result.getAppointmentDate());
        assertEquals(startTime, result.getAppointmentStartTime());
        assertEquals(LocalTime.of(11, 0), result.getAppointmentEndTime());

        assertEquals(60, result.getTotalDuration());

        assertEquals(new BigDecimal("1500"), result.getPrice());

        assertEquals(AppointmentStatus.PENDING, result.getStatus());

        assertEquals(client, result.getClient());
        assertEquals(artistProfile, result.getArtistProfile());

        assertEquals(List.of(beautyService), result.getBeautyServices());

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectPastDate() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        pastDate,
                        startTime,
                        List.of(1L),
                        null,
                        null
                )
        );
    }

    @Test
    void create_shouldRejectUserWhoIsNotClient() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);

        User user = new User();
        user.setId(clientId);
        user.setRole(Role.ARTIST);

        when(userService.getById(clientId)).thenReturn(user);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(1L),
                        null,
                        null
                ));
    }

    @Test
    void create_shouldRejectInactiveBeautyService() {

        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10,0);

        User client = new User();
        client.setId(clientId);
        client.setRole(Role.CLIENT);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(artistProfileId);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(LocalTime.of(8, 0));
        workingSchedule.setEndTime(LocalTime.of(15, 0));
        workingSchedule.setAvailable(true);

        BeautyService beautyService = new BeautyService();
        beautyService.setId(1L);
        beautyService.setDuration(60);
        beautyService.setPrice(new BigDecimal("1500"));
        beautyService.setActive(false);

        when(userService.getById(clientId)).thenReturn(client);

        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);

        when(workingScheduleService.findActiveByArtistAndDay(
                artistProfileId,
                date.getDayOfWeek()
        )).thenReturn(workingSchedule);

        when(beautyServiceService.getById(1L)).thenReturn(beautyService);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(1L),
                        null,
                        null
                ));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectAppointmentOutsideWorkingHours() {

        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(14, 30);

        User client = new User();
        client.setId(clientId);
        client.setRole(Role.CLIENT);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(artistProfileId);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(LocalTime.of(8,0));
        workingSchedule.setEndTime(LocalTime.of(15,0));
        workingSchedule.setAvailable(true);

        BeautyService service = new BeautyService();
        service.setId(1L);
        service.setDuration(60);
        service.setPrice(new BigDecimal("1500"));
        service.setActive(true);

        when(userService.getById(clientId)).thenReturn(client);

        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);

        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);

        when(beautyServiceService.getById(1L)).thenReturn(service);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(1L),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectOverlappingAppointment() {

        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 30);

        User client = new User();
        client.setId(clientId);
        client.setRole(Role.CLIENT);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(artistProfileId);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(LocalTime.of(8, 0));
        workingSchedule.setEndTime(LocalTime.of(15, 0));
        workingSchedule.setAvailable(true);

        BeautyService beautyService = new BeautyService();
        beautyService.setId(1L);
        beautyService.setDuration(60);
        beautyService.setPrice(new BigDecimal("1500"));
        beautyService.setActive(true);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentStartTime(LocalTime.of(10, 0));
        existingAppointment.setAppointmentEndTime(LocalTime.of(11, 0));

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(beautyService);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of(existingAppointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(1L),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectAppointmentWhenArtistDoesNotWorkThatDay() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);

        User client = new User();
        client.setId(clientId);
        client.setRole(Role.CLIENT);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(artistProfileId);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(null);
        workingSchedule.setEndTime(null);
        workingSchedule.setAvailable(true);

        when(userService.getById(clientId)).thenReturn(client);

        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);

        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(1L),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectAppointmentWithNoBeautyServices() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectNonExistingBeautyService() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);

        User client = new User();
        client.setId(clientId);
        client.setRole(Role.CLIENT);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(artistProfileId);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(LocalTime.of(8, 0));
        workingSchedule.setEndTime(LocalTime.of(15, 0));
        workingSchedule.setAvailable(true);

        when(userService.getById(clientId)).thenReturn(client);

        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);

        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);

        when(beautyServiceService.getById(999L)).thenThrow(new BeautyServiceNotFoundException(999L));

        assertThrows(BeautyServiceNotFoundException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(999L),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectNullDate() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalTime startTime = LocalTime.of(10, 0);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        null,
                        startTime,
                        List.of(1L),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectNullStartTime() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        null,
                        List.of(1L),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}

