package com.makeup.booking;

import com.makeup.booking.model.*;
import com.makeup.booking.model.enums.AppointmentStatus;
import com.makeup.booking.model.enums.Role;
import com.makeup.booking.model.exceptions.AppointmentNotFoundException;
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
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

    @Test
    void create_shouldAllowAppointmentEndingExactlyAtWorkingHoursEnd() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(14, 0);

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

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(beautyService);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        assertEquals(LocalTime.of(14, 0), result.getAppointmentStartTime());
        assertEquals(LocalTime.of(15, 0), result.getAppointmentEndTime());

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void create_shouldAllowAppointmentStartingExactlyWhenExistingAppointmentEnds() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(11, 0);

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
        beautyService.setPrice(new BigDecimal("1500.00"));
        beautyService.setActive(true);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentStartTime(LocalTime.of(10, 0));
        existingAppointment.setAppointmentEndTime(LocalTime.of(11, 0));

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(beautyService);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of(existingAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        assertEquals(LocalTime.of(11, 0), result.getAppointmentStartTime());
        assertEquals(LocalTime.of(12, 0), result.getAppointmentEndTime());

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void create_shouldAllowAppointmentEndingExactlyWhenExistingAppointmentStarts() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(9, 0);

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
        beautyService.setPrice(new BigDecimal("1500.00"));
        beautyService.setActive(true);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentStartTime(LocalTime.of(10, 0));
        existingAppointment.setAppointmentEndTime(LocalTime.of(11, 0));

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(beautyService);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of(existingAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        assertEquals(LocalTime.of(9, 0), result.getAppointmentStartTime());
        assertEquals(LocalTime.of(10, 0), result.getAppointmentEndTime());

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectAppointmentOverlappingAtStart() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(9, 30);

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
        beautyService.setPrice(new BigDecimal("1500.00"));
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
    void create_shouldRejectAppointmentOverlappingAtEnd() {
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
        beautyService.setPrice(new BigDecimal("1500.00"));
        beautyService.setActive(true);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentStartTime(LocalTime.of(10, 0));
        existingAppointment.setAppointmentEndTime(LocalTime.of(11, 0));

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(beautyService);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of(existingAppointment));

        assertThrows(
                IllegalArgumentException.class,
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
    void create_shouldRejectAppointmentThatStartsAndEndsDuringExistingAppointment() {
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

        BeautyService beautyService = new BeautyService();
        beautyService.setId(1L);
        beautyService.setDuration(60);
        beautyService.setPrice(new BigDecimal("1500.00"));
        beautyService.setActive(true);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentStartTime(LocalTime.of(10, 0));
        existingAppointment.setAppointmentEndTime(LocalTime.of(11, 0));

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(beautyService);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of(existingAppointment));

        assertThrows(
                IllegalArgumentException.class,
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
    void create_shouldRejectAppointmentThatOverlapsWithExistingAppointment() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(9, 0);

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
        beautyService.setDuration(180);
        beautyService.setPrice(new BigDecimal("5500.00"));
        beautyService.setActive(true);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentStartTime(LocalTime.of(10, 0));
        existingAppointment.setAppointmentEndTime(LocalTime.of(11, 0));

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(beautyService);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of(existingAppointment));

        assertThrows(
                IllegalArgumentException.class,
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
    void create_shouldRejectAppointmentThatIsInsideExistingAppointment() {
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

        BeautyService beautyService = new BeautyService();
        beautyService.setId(1L);
        beautyService.setDuration(60);
        beautyService.setPrice(new BigDecimal("1500.00"));
        beautyService.setActive(true);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentStartTime(LocalTime.of(9, 0));
        existingAppointment.setAppointmentEndTime(LocalTime.of(12, 0));

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(beautyService);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of(existingAppointment));

        assertThrows(
                IllegalArgumentException.class,
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
    void create_shouldCalculateTotalDurationAndPriceForMultipleServices() {
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

        BeautyService service1 = new BeautyService();
        service1.setId(1L);
        service1.setDuration(60);
        service1.setPrice(new BigDecimal("1500.00"));
        service1.setActive(true);

        BeautyService service2 = new BeautyService();
        service2.setId(2L);
        service2.setDuration(90);
        service2.setPrice(new BigDecimal("2500.00"));
        service2.setActive(true);

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(service1);
        when(beautyServiceService.getById(2L)).thenReturn(service2);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.create(
                clientId,
                artistProfileId,
                date,
                startTime,
                List.of(1L, 2L),
                null,
                null
        );

        assertNotNull(result);

        assertEquals(150, result.getTotalDuration());
        assertEquals(new BigDecimal("4000.00"), result.getPrice());
        assertEquals(LocalTime.of(12, 30), result.getAppointmentEndTime());
        assertEquals(List.of(service1, service2), result.getBeautyServices());

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectAppointmentWhenOneOfMultipleServicesIsInactive() {
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

        BeautyService activeService = new BeautyService();
        activeService.setId(1L);
        activeService.setDuration(60);
        activeService.setPrice(new BigDecimal("1500.00"));
        activeService.setActive(true);

        BeautyService inactiveService = new BeautyService();
        inactiveService.setId(2L);
        inactiveService.setDuration(90);
        inactiveService.setPrice(new BigDecimal("2500.00"));
        inactiveService.setActive(false);

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);

        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);

        when(beautyServiceService.getById(1L)).thenReturn(activeService);

        when(beautyServiceService.getById(2L)).thenReturn(inactiveService);

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(1L, 2L),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }


    @Test
    void create_shouldRejectAppointmentWhenOneOfMultipleServicesDoesNotExist() {
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

        BeautyService existingService = new BeautyService();
        existingService.setId(1L);
        existingService.setDuration(60);
        existingService.setPrice(new BigDecimal("1500.00"));
        existingService.setActive(true);

        when(userService.getById(clientId)).thenReturn(client);
        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);
        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);
        when(beautyServiceService.getById(1L)).thenReturn(existingService);
        when(beautyServiceService.getById(999L)).thenThrow(new BeautyServiceNotFoundException(999L));

        assertThrows(
                BeautyServiceNotFoundException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(1L, 999L),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void create_shouldRejectDuplicateBeautyServices() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.create(
                        clientId,
                        artistProfileId,
                        date,
                        startTime,
                        List.of(1L, 1L),
                        null,
                        null
                )
        );

        verify(appointmentRepository, never())
                .save(any(Appointment.class));
    }

    @Test
    void create_shouldAllowMultipleServicesEndingExactlyAtWorkingHoursEnd() {
        Long clientId = 1L;
        Long artistProfileId = 1L;

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(13, 0);

        User client = new User();
        client.setId(clientId);
        client.setRole(Role.CLIENT);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(artistProfileId);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(LocalTime.of(8, 0));
        workingSchedule.setEndTime(LocalTime.of(15, 0));
        workingSchedule.setAvailable(true);

        BeautyService service1 = new BeautyService();
        service1.setId(1L);
        service1.setDuration(60);
        service1.setPrice(new BigDecimal("1500.00"));
        service1.setActive(true);

        BeautyService service2 = new BeautyService();
        service2.setId(2L);
        service2.setDuration(60);
        service2.setPrice(new BigDecimal("2500.00"));
        service2.setActive(true);

        when(userService.getById(clientId)).thenReturn(client);

        when(artistProfileService.getById(artistProfileId)).thenReturn(artistProfile);

        when(workingScheduleService.findActiveByArtistAndDay(artistProfileId, date.getDayOfWeek())).thenReturn(workingSchedule);

        when(beautyServiceService.getById(1L)).thenReturn(service1);

        when(beautyServiceService.getById(2L)).thenReturn(service2);

        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(artistProfileId, date)).thenReturn(List.of());

        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.create(
                clientId,
                artistProfileId,
                date,
                startTime,
                List.of(1L, 2L),
                null,
                null
        );

        assertNotNull(result);

        assertEquals(LocalTime.of(13, 0), result.getAppointmentStartTime());

        assertEquals(LocalTime.of(15, 0), result.getAppointmentEndTime());

        assertEquals(120, result.getTotalDuration());

        assertEquals(new BigDecimal("4000.00"), result.getPrice());

        assertEquals(List.of(service1, service2), result.getBeautyServices());

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    public void updateStatus_shouldConfirmPendingAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.updateStatus(1L, AppointmentStatus.CONFIRMED);

        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());

        verify(appointmentRepository).save(appointment);
    }

    @Test
    public void updateStatus_shouldRejectPendingAppointmentToCompleted() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateStatus(
                        1L,
                        AppointmentStatus.COMPLETED
                )
        );

        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    public void updateStatus_shouldCancelPendingAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.updateStatus(
                1L,
                AppointmentStatus.CANCELLED
        );

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());

        verify(appointmentRepository).save(appointment);
    }

    @Test
    public void updateStatus_shouldCompleteConfirmedAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.updateStatus(1L, AppointmentStatus.COMPLETED);

        assertEquals(AppointmentStatus.COMPLETED, result.getStatus());

        verify(appointmentRepository).save(appointment);
    }

    @Test
    public void updateStatus_shouldCancelConfirmedAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.updateStatus(
                1L,
                AppointmentStatus.CANCELLED
        );

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());

        verify(appointmentRepository).save(appointment);
    }

    @Test
    public void updateStatus_shouldAcceptConfirmedAppointmentToNoShow() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.updateStatus(
                1L,
                AppointmentStatus.NO_SHOW
        );

        assertEquals(AppointmentStatus.NO_SHOW, result.getStatus());

        verify(appointmentRepository).save(appointment);
    }

    @Test
    public void updateStatus_shouldRejectChangingCompletedAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateStatus(
                        1L,
                        AppointmentStatus.CANCELLED
                )
        );

        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    public void updateStatus_shouldRejectNullStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateStatus(1L, null));

        verify(appointmentRepository, never()).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void updateStatus_shouldRejectNonExistingAppointment() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class,
                () -> appointmentService.updateStatus(
                        999L,
                        AppointmentStatus.CONFIRMED
                ));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void cancel_shouldRejectAlreadyCancelledAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.cancel(1L)
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void cancel_shouldCancelPendingAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        appointmentService.cancel(1L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());

        verify(appointmentRepository).save(appointment);
    }

    @Test
    public void cancel_shouldRejectNonExistingAppointment() {
        when(appointmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class,
                () -> appointmentService.cancel(999L));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void reschedule_shouldRescheduleAppointmentSuccessfully() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(10L);
        appointment.setArtistProfile(artistProfile);

        BeautyService beautyService = new BeautyService();
        beautyService.setDuration(120);

        appointment.setBeautyServices(List.of(beautyService));

        LocalDate newDate = LocalDate.now().plusDays(1);
        LocalTime newStartTime = LocalTime.of(12, 0);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(LocalTime.of(8, 0));
        workingSchedule.setEndTime(LocalTime.of(17, 0));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(workingScheduleService.findActiveByArtistAndDay(10L, newDate.getDayOfWeek())).thenReturn(workingSchedule);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(10L, newDate)).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.reschedule(1L, newDate, newStartTime);

        assertEquals(newDate, result.getAppointmentDate());
        assertEquals(newStartTime, result.getAppointmentStartTime());
        assertEquals(LocalTime.of(14, 0), result.getAppointmentEndTime());

        verify(appointmentRepository).save(appointment);
    }

    @Test
    public void reschedule_shouldRejectNullDateOrTime() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        null,
                        LocalTime.of(10, 0)
                )
        );

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        LocalDate.now().plusDays(1),
                        null
                )
        );
    }

    @Test
    public void reschedule_shouldRejectCancelledAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void reschedule_shouldRejectCompletedAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void reschedule_shouldRejectPastDate() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        LocalDate.now().minusDays(1),
                        LocalTime.of(10, 0)
                ));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void reschedule_shouldRejectPastTimeToday() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        LocalDate.now(),
                        LocalTime.now().minusMinutes(1)
                ));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void reschedule_shouldRejectWhenArtistDoesNotWorkThatDay() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(10L);
        appointment.setArtistProfile(artistProfile);

        LocalDate newDate = LocalDate.now().plusDays(1);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(null);
        workingSchedule.setEndTime(null);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        when(workingScheduleService.findActiveByArtistAndDay(10L, newDate.getDayOfWeek())).thenReturn(workingSchedule);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        newDate,
                        LocalTime.of(10, 0)
                ));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void reschedule_shouldRejectAppointmentOutsideWorkingHours() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(10L);
        appointment.setArtistProfile(artistProfile);

        BeautyService service = new BeautyService();
        service.setDuration(120);
        appointment.setBeautyServices(List.of(service));

        LocalDate newDate = LocalDate.now().plusDays(1);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(LocalTime.of(9, 0));
        workingSchedule.setEndTime(LocalTime.of(17, 0));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        when(workingScheduleService.findActiveByArtistAndDay(10L, newDate.getDayOfWeek())).thenReturn(workingSchedule);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        newDate,
                        LocalTime.of(8, 0)
                ));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        newDate,
                        LocalTime.of(16, 0)
                ));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void reschedule_shouldRejectAppointmentThatOverlapsWithExisting() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(10L);

        appointment.setArtistProfile(artistProfile);

        BeautyService service = new BeautyService();
        service.setDuration(60);
        appointment.setBeautyServices(List.of(service));

        LocalDate newDate = LocalDate.now().plusDays(1);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(LocalTime.of(9, 0));
        workingSchedule.setEndTime(LocalTime.of(17, 0));

        Appointment existingAppointment = new Appointment();
        existingAppointment.setId(2L);
        existingAppointment.setAppointmentDate(newDate);
        existingAppointment.setAppointmentStartTime(LocalTime.of(10, 0));
        existingAppointment.setAppointmentEndTime(LocalTime.of(11, 0));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(workingScheduleService.findActiveByArtistAndDay(10L, newDate.getDayOfWeek())).thenReturn(workingSchedule);
        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(10L, newDate)).thenReturn(List.of(existingAppointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.reschedule(
                        1L,
                        newDate,
                        LocalTime.of(10, 30)
                )
        );

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void reschedule_shouldIgnoreTheSameAppointmentWhenCheckingOverlap() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(10L);
        appointment.setArtistProfile(artistProfile);

        BeautyService service = new BeautyService();
        service.setDuration(60);
        appointment.setBeautyServices(List.of(service));

        LocalDate newDate = LocalDate.now().plusDays(1);
        LocalTime newStartTime = LocalTime.of(10, 0);

        WorkingSchedule workingSchedule = new WorkingSchedule();
        workingSchedule.setStartTime(LocalTime.of(9, 0));
        workingSchedule.setEndTime(LocalTime.of(17, 0));

        Appointment sameAppointment = new Appointment();
        sameAppointment.setId(1L);
        sameAppointment.setAppointmentStartTime(LocalTime.of(10, 0));
        sameAppointment.setAppointmentEndTime(LocalTime.of(11, 0));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        when(workingScheduleService.findActiveByArtistAndDay(10L, newDate.getDayOfWeek())).thenReturn(workingSchedule);

        when(appointmentRepository.findByArtistProfileIdAndAppointmentDate(10L, newDate)).thenReturn(List.of(sameAppointment));

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.reschedule(
                1L,
                newDate,
                newStartTime
        );

        assertEquals(newDate, result.getAppointmentDate());
        assertEquals(newStartTime, result.getAppointmentStartTime());
        assertEquals(LocalTime.of(11, 0), result.getAppointmentEndTime());

        verify(appointmentRepository).save(appointment);
    }
}

