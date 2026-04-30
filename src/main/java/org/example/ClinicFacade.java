package org.example;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ClinicFacade {

    private final DoctorRepository doctorRepo = new DoctorRepository();
    private final PatientRepository patientRepo = new PatientRepository();
    private final AppointmentRepository appointmentRepo = new AppointmentRepository();

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(17, 0);
    private static final int SLOT_MINUTES = 30;

    // Register a new patient
    public int registerPatient(String name, String phone) {
        return patientRepo.addPatient(name, phone);
    }

    // Get all doctors by specialization
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepo.findBySpecialization(specialization);
    }

    // Get all available specializations
    public List<String> getAllSpecializations() {
        return doctorRepo.getAllSpecializations();
    }

    public boolean patientExists(int patientId) {
        return patientRepo.exists(patientId);
    }

    // Book an appointment for a patient with a doctor
    public boolean bookAppointment(int patientId, int doctorId, String dateTime) {
        return appointmentRepo.book(doctorId, patientId, dateTime);
    }

    // Get all appointments for a patient
    public List<Appointment> getAppointmentsForPatient(int patientId) {
        return appointmentRepo.getForPatient(patientId);
    }

    // Get all appointments for a doctor
    public List<Appointment> getAppointmentsForDoctor(int doctorId) {
        return appointmentRepo.getForDoctor(doctorId);
    }

    public void cancelAppointment(int appointmentId) {
        Appointment a = appointmentRepo.getById(appointmentId);
        if (a == null) {
            System.out.println("Appointment not found.");
            return;
        }

        a.cancel(); // State pattern
        appointmentRepo.updateStatus(appointmentId, a.getStatus());
    }

    public void completeAppointment(int appointmentId) {
        Appointment a = appointmentRepo.getById(appointmentId);
        if (a == null) {
            System.out.println("Appointment not found.");
            return;
        }

        a.complete(); // State pattern
        appointmentRepo.updateStatus(appointmentId, a.getStatus());
    }

    public boolean doctorExists(int doctorId) {
        return doctorRepo.exists(doctorId);
    }

    public int addDoctor(String name, String specialization) {
        return doctorRepo.addDoctor(name, specialization);
    }

    public List<LocalTime> getAvailableSlots(int doctorId, LocalDate date) {

        // If it is a weekend, return an empty list
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return new ArrayList<>();
        }

        // Get the doctor to access working schedule
        Doctor doctor = doctorRepo.getById(doctorId);

        LocalTime workStart = doctor.getWorkStart();
        LocalTime workEnd = doctor.getWorkEnd();
        int slotMinutes = doctor.getSlotMinutes();

        // Get already booked appointments for the selected date
        List<Appointment> booked = appointmentRepo.getForDoctorOnDate(doctorId, date);

        List<LocalTime> bookedTimes = new ArrayList<>();
        for (Appointment a : booked) {
            // Free the slot if the appointment is cancelled or completed
            String status = a.getStatus();
            if ("CANCELLED".equals(status) || "COMPLETED".equals(status)) {
                continue;
            }
            bookedTimes.add(a.getDateTime().toLocalTime());
        }

        // Generate available working slots
        List<LocalTime> available = new ArrayList<>();

        LocalTime time = workStart;

        while (time.isBefore(workEnd)) {
            // If the slot is not occupied, add it
            if (!bookedTimes.contains(time)) {
                available.add(time);
            }

            time = time.plusMinutes(slotMinutes); // move to the next slot
        }

        return available;
    }

    public boolean bookAppointmentInSlot(int patientId,
                                         int doctorId,
                                         LocalDate date,
                                         LocalTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return appointmentRepo.book(doctorId, patientId, dateTime.toString());
    }
}