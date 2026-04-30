package org.example;

import org.example.dto.AppointmentDetails;
import org.example.service.OperationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClinicFacadeTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void clearDbProperties() {
        System.clearProperty("clinic.db.path");
        System.clearProperty("clinic.db.url");
    }

    @Test
    void initDoesNotDestroyExistingData() {
        useTempDatabase("persistence.db");
        DatabaseInitializer.init();

        ClinicFacade facade = new ClinicFacade();
        int patientId = facade.registerPatient("Alice", "+7-777-000-0000");

        DatabaseInitializer.init();

        assertTrue(facade.patientExists(patientId));
    }

    @Test
    void doctorCannotBeDoubleBookedForSameSlot() {
        useTempDatabase("double-booking.db");
        DatabaseInitializer.init();

        ClinicFacade facade = new ClinicFacade();
        int patientOneId = facade.registerPatient("Alice", "+7-777-000-0001");
        int patientTwoId = facade.registerPatient("Bob", "+7-777-000-0002");
        int doctorId = facade.addDoctor("Dr. Smith", "cardiologist");

        LocalDate appointmentDate = nextWeekday();
        LocalTime appointmentTime = LocalTime.of(9, 0);

        assertTrue(facade.bookAppointmentInSlot(patientOneId, doctorId, appointmentDate, appointmentTime));
        assertFalse(facade.bookAppointmentInSlot(patientTwoId, doctorId, appointmentDate, appointmentTime));
        assertEquals(1, facade.getAppointmentsForDoctor(doctorId).size());
    }

    @Test
    void patientCanOnlyCancelOwnAppointment() {
        useTempDatabase("patient-ownership.db");
        DatabaseInitializer.init();

        ClinicFacade facade = new ClinicFacade();
        int patientOneId = facade.registerPatient("Alice", "+7-777-000-0010");
        int patientTwoId = facade.registerPatient("Bob", "+7-777-000-0020");
        int doctorId = facade.addDoctor("Dr. House", "therapist");

        LocalDate appointmentDate = nextWeekday();
        LocalTime appointmentTime = LocalTime.of(10, 0);
        assertTrue(facade.bookAppointmentInSlot(patientOneId, doctorId, appointmentDate, appointmentTime));

        List<Appointment> appointments = facade.getAppointmentsForPatient(patientOneId);
        int appointmentId = appointments.get(0).getId();

        assertFalse(facade.cancelAppointmentForPatient(appointmentId, patientTwoId));
        assertEquals("CONFIRMED", facade.getAppointmentsForPatient(patientOneId).get(0).getStatus());
        assertTrue(facade.cancelAppointmentForPatient(appointmentId, patientOneId));
        assertEquals("CANCELLED", facade.getAppointmentsForPatient(patientOneId).get(0).getStatus());
    }

    @Test
    void doctorCanOnlyCompleteOwnAppointment() {
        useTempDatabase("doctor-ownership.db");
        DatabaseInitializer.init();

        ClinicFacade facade = new ClinicFacade();
        int patientId = facade.registerPatient("Alice", "+7-777-000-0030");
        int doctorOneId = facade.addDoctor("Dr. Carter", "surgeon");
        int doctorTwoId = facade.addDoctor("Dr. Wilson", "cardiologist");

        LocalDate appointmentDate = nextWeekday();
        LocalTime appointmentTime = LocalTime.of(11, 0);
        assertTrue(facade.bookAppointmentInSlot(patientId, doctorOneId, appointmentDate, appointmentTime));

        int appointmentId = facade.getAppointmentsForDoctor(doctorOneId).get(0).getId();

        assertFalse(facade.completeAppointmentForDoctor(appointmentId, doctorTwoId));
        assertEquals("CONFIRMED", facade.getAppointmentsForDoctor(doctorOneId).get(0).getStatus());
        assertTrue(facade.completeAppointmentForDoctor(appointmentId, doctorOneId));
        assertEquals("COMPLETED", facade.getAppointmentsForDoctor(doctorOneId).get(0).getStatus());
    }

    @Test
    void pastDatesAreRejected() {
        useTempDatabase("past-date.db");
        DatabaseInitializer.init();

        ClinicFacade facade = new ClinicFacade();
        int patientId = facade.registerPatient("Alice", "+7-777-000-0040");
        int doctorId = facade.addDoctor("Dr. Brown", "therapist");

        assertFalse(facade.bookAppointmentInSlot(
                patientId,
                doctorId,
                LocalDate.now().minusDays(1),
                LocalTime.of(9, 0)
        ));
    }

    @Test
    void doctorScheduleCanBeUpdatedAndAffectsAvailableSlots() {
        useTempDatabase("doctor-schedule.db");
        DatabaseInitializer.init();

        ClinicFacade facade = new ClinicFacade();
        int doctorId = facade.addDoctor("Dr. Patel", "therapist");

        OperationResult<Void> result = facade.updateDoctorSchedule(
                doctorId,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                60
        );

        assertTrue(result.isSuccess());
        Doctor doctor = facade.getDoctorById(doctorId);
        assertNotNull(doctor);
        assertEquals(LocalTime.of(8, 0), doctor.getWorkStart());
        assertEquals(LocalTime.of(10, 0), doctor.getWorkEnd());
        assertEquals(60, doctor.getSlotMinutes());
        assertEquals(List.of(LocalTime.of(8, 0), LocalTime.of(9, 0)), facade.getAvailableSlots(doctorId, nextWeekday()));
    }

    @Test
    void appointmentDetailsIncludeJoinedNames() {
        useTempDatabase("appointment-details.db");
        DatabaseInitializer.init();

        ClinicFacade facade = new ClinicFacade();
        int patientId = facade.registerPatient("Alice", "+7-777-000-1111");
        int doctorId = facade.addDoctor("Dr. Stone", "cardiologist");
        LocalDate appointmentDate = nextWeekday();
        LocalTime appointmentTime = LocalTime.of(9, 0);

        assertTrue(facade.bookAppointmentInSlot(patientId, doctorId, appointmentDate, appointmentTime));

        AppointmentDetails details = facade.getAppointmentDetailsForPatient(patientId).get(0);
        assertEquals("Dr. Stone", details.getDoctorName());
        assertEquals("Alice", details.getPatientName());
        assertEquals("cardiologist", details.getDoctorSpecialization());
        assertEquals("CONFIRMED", details.getStatus());
    }

    @Test
    void invalidScheduleUpdateReturnsClearFailureMessage() {
        useTempDatabase("invalid-schedule.db");
        DatabaseInitializer.init();

        ClinicFacade facade = new ClinicFacade();
        int doctorId = facade.addDoctor("Dr. Reed", "surgeon");

        OperationResult<Void> result = facade.updateDoctorSchedule(
                doctorId,
                LocalTime.of(17, 0),
                LocalTime.of(9, 0),
                30
        );

        assertFalse(result.isSuccess());
        assertEquals("Work start must be earlier than work end.", result.getMessage());
    }

    private void useTempDatabase(String fileName) {
        System.setProperty("clinic.db.path", tempDir.resolve(fileName).toString());
    }

    private LocalDate nextWeekday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.plusDays(1);
        }
        return date;
    }
}
